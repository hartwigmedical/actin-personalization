package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.Diagnosis
import com.hartwig.actin.personalization.datamodel.DistantMetastasesStatus
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.LocationGroup
import com.hartwig.actin.personalization.datamodel.PatientRecord
import com.hartwig.actin.personalization.datamodel.Treatment

typealias DiagnosisAndEpisode = Pair<Diagnosis, Episode>

private fun Episode.doesNotIncludeAdjuvantOrNeoadjuvantTreatment(): Boolean {
    return !hasHadPreSurgerySystemicChemotherapy &&
            !hasHadPostSurgerySystemicChemotherapy &&
            !hasHadPreSurgerySystemicTargetedTherapy &&
            !hasHadPostSurgerySystemicTargetedTherapy
}

class PatientPopulationBreakdown(
    private val subPopulations: List<SubPopulation>
) {
    fun analyze(): List<SubPopulationAnalysis> {
        val treatments = subPopulations.first().patientsByTreatment.map { it.first }
        return subPopulations.map { subPopulation ->
            SubPopulationAnalysis(
                subPopulation.name,
                MeasurementType.entries.associateWith { calculateMeasurements(it.calculation, subPopulation) },
                treatments
            )
        }
    }

    private fun calculateMeasurements(calculation: Calculation, subPopulation: SubPopulation): TreatmentMeasurementCollection {
        val eligibleSubPopulationSize = subPopulation.patients.count(calculation::isEligible)
        return TreatmentMeasurementCollection(
            subPopulation.patientsByTreatment.associate { (treatment, patients) ->
                treatment to calculation.calculate(patients, eligibleSubPopulationSize)
            },
            eligibleSubPopulationSize
        )
    }


    companion object {
        fun createForCriteria(
            patients: List<PatientRecord>, age: Int, whoStatus: Int, hasRasMutation: Boolean, metastasisLocationGroups: Set<LocationGroup>
        ): PatientPopulationBreakdown {
            val referencePop = patients.flatMap(PatientRecord::tumorEntries).map { (diagnosis, episodes) ->
                diagnosis to episodes.single { it.order == 1 }
            }
                .filter { (_, episode) ->
                    episode.distantMetastasesStatus == DistantMetastasesStatus.AT_START &&
                            episode.systemicTreatmentPlan?.treatment?.let { it != Treatment.OTHER } == true &&
                            episode.surgeries.isEmpty() &&
                            episode.doesNotIncludeAdjuvantOrNeoadjuvantTreatment()
                }

            val patientsByTreatment = referencePop.groupBy { (_, episode) -> episode.systemicTreatmentPlan!!.treatment }.entries
                .sortedByDescending { it.value.size }

            val subPopulations = SubPopulationDefinition.createAllForPatientProfile(
                age, whoStatus, hasRasMutation, metastasisLocationGroups
            ).map { (title, criteria) ->
                val subPopulationByTreatment = patientsByTreatment.map { (treatment, patients) -> treatment to patients.filter(criteria) }
                SubPopulation(
                    title, subPopulationByTreatment.flatMap { it.second }, subPopulationByTreatment
                )
            }

            return PatientPopulationBreakdown(subPopulations)
        }
    }

}