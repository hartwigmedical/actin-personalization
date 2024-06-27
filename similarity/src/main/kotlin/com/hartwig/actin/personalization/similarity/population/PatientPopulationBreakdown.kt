package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.Diagnosis
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.Treatment

typealias DiagnosisAndEpisode = Pair<Diagnosis, Episode>

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
            patientsByTreatment: List<Map.Entry<Treatment, List<DiagnosisAndEpisode>>>,
            subPopulationDefinitions: List<SubPopulationDefinition>
        ): PatientPopulationBreakdown {
            val subPopulations = subPopulationDefinitions.map { (title, criteria) ->
                val subPopulationByTreatment = patientsByTreatment.map { (treatment, patients) -> treatment to patients.filter(criteria) }
                SubPopulation(title, subPopulationByTreatment.flatMap { it.second }, subPopulationByTreatment)
            }
            return PatientPopulationBreakdown(subPopulations)
        }
    }
}