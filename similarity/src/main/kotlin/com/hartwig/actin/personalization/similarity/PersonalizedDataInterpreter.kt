package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.DiagnosisEpisodeTreatment
import com.hartwig.actin.personalization.datamodel.MetastasesDetectionStatus
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.LocationGroup
import com.hartwig.actin.personalization.datamodel.ReferencePatient

import com.hartwig.actin.personalization.datamodel.TreatmentGroup
import com.hartwig.actin.personalization.datamodel.serialization.ReferencePatientJson

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

class PersonalizedDataInterpreter(val patientsByTreatment: List<Pair<TreatmentGroup, List<DiagnosisEpisodeTreatment>>>) {

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
            val referencePop = patients.flatMap(ReferencePatient::tumorEntries).mapNotNull { (diagnosis, episodes) ->
                // Retrieve the single episode with order == 1 and create DiagnosisEpisodeTreatment
                val episode = episodes.singleOrNull { it.order == 1 } ?: return@mapNotNull null
                if (episode.distantMetastasesDetectionStatus == MetastasesDetectionStatus.AT_START &&
                    episode.surgeries.isEmpty() &&
                    episode.doesNotIncludeAdjuvantOrNeoadjuvantTreatment()
                ) {
                    DiagnosisEpisodeTreatment(diagnosis, episode, episode.systemicTreatmentPlan)
                } else null
            }

            val patientsByTreatment = referencePop.groupBy { it.systemicTreatmentPlan?.treatment?.treatmentGroup ?: TreatmentGroup.NONE }
                .toList()
                .sortedByDescending { it.second.size }

            return PersonalizedDataInterpreter(patientsByTreatment)
        }


    }
}