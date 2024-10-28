package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.MetastasesDetectionStatus
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.LocationGroup
import com.hartwig.actin.personalization.datamodel.ReferencePatient
import com.hartwig.actin.personalization.datamodel.Treatment
import com.hartwig.actin.personalization.datamodel.TreatmentGroup
import com.hartwig.actin.personalization.datamodel.serialization.ReferencePatientJson
import com.hartwig.actin.personalization.similarity.population.DiagnosisAndEpisode
import com.hartwig.actin.personalization.similarity.population.PatientPopulationBreakdown
import com.hartwig.actin.personalization.similarity.population.PersonalizedDataAnalysis
import io.github.oshai.kotlinlogging.KotlinLogging
import com.hartwig.actin.personalization.similarity.population.PopulationDefinition

private fun Episode.doesNotIncludeAdjuvantOrNeoadjuvantTreatment(): Boolean {
    return !hasHadPreSurgerySystemicChemotherapy &&
            !hasHadPostSurgerySystemicChemotherapy &&
            !hasHadPreSurgerySystemicTargetedTherapy &&
            !hasHadPostSurgerySystemicTargetedTherapy
}

class PersonalizedDataInterpreter(val patientsByTreatment: List<Pair<TreatmentGroup, List<DiagnosisAndEpisode>>>) {

    fun analyzePatient(
        age: Int, whoStatus: Int, hasRasMutation: Boolean, metastasisLocationGroups: Set<LocationGroup>
    ): PersonalizedDataAnalysis {
        val populationDefinitions =
            PopulationDefinition.createAllForPatientProfile(age, whoStatus, hasRasMutation, metastasisLocationGroups)

        return PatientPopulationBreakdown(patientsByTreatment, populationDefinitions).analyze()
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}

        fun createFromFile(path: String): PersonalizedDataInterpreter {
            LOGGER.info { "Loading patient records from file $path" }
            val patients = ReferencePatientJson.read(path)
            LOGGER.info { " Loaded ${patients.size} patient records" }
            return createFromReferencePatients(patients)
        }

        fun createFromReferencePatients(patients: List<ReferencePatient>): PersonalizedDataInterpreter {
            val referencePop = patients.flatMap(ReferencePatient::tumorEntries).map { (diagnosis, episodes) ->
                diagnosis to episodes.single { it.order == 1 }
            }
                .filter { (_, episode) ->
                    episode.distantMetastasesDetectionStatus == MetastasesDetectionStatus.AT_START &&
                            episode.surgeries.isEmpty() &&
                            episode.doesNotIncludeAdjuvantOrNeoadjuvantTreatment()
                }

            val patientsByTreatment = referencePop.groupBy { (_, episode) ->
                episode.systemicTreatmentPlan?.treatment?.treatmentGroup ?: TreatmentGroup.NONE
            }
                .toList()
                .sortedByDescending { it.second.size }

            return PersonalizedDataInterpreter(patientsByTreatment)
        }
    }
}