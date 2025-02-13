package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.diagnosis.LocationGroup
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastasesDetectionStatus
import com.hartwig.actin.personalization.datamodel.old.DiagnosisEpisode
import com.hartwig.actin.personalization.datamodel.old.Episode
import com.hartwig.actin.personalization.datamodel.old.ReferencePatient
import com.hartwig.actin.personalization.datamodel.serialization.ReferencePatientJson
import com.hartwig.actin.personalization.datamodel.treatment.Treatment
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentGroup
import com.hartwig.actin.personalization.similarity.population.PatientPopulationBreakdown
import com.hartwig.actin.personalization.similarity.population.PersonalizedDataAnalysis
import com.hartwig.actin.personalization.similarity.population.PopulationDefinition
import io.github.oshai.kotlinlogging.KotlinLogging

private fun Episode.doesNotIncludeAdjuvantOrNeoadjuvantTreatment(): Boolean {
    return !hasHadPreSurgerySystemicChemotherapy &&
            !hasHadPostSurgerySystemicChemotherapy &&
            !hasHadPreSurgerySystemicTargetedTherapy &&
            !hasHadPostSurgerySystemicTargetedTherapy
}

class PersonalizedDataInterpreter(val patientsByTreatment: List<Pair<TreatmentGroup, List<DiagnosisEpisode>>>) {

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
            LOGGER.info { "Loading reference patient database from $path" }
            val patients = ReferencePatientJson.read(path)

            LOGGER.info { " Loaded ${patients.size} reference patients" }
            // TODO (KD) Fix
            return createFromReferencePatients(emptyList())
        }

        fun createFromReferencePatients(patients: List<ReferencePatient>): PersonalizedDataInterpreter {
            val referencePop = patients
                .flatMap(ReferencePatient::tumorEntries)
                .map { (diagnosis, episodes) -> DiagnosisEpisode(diagnosis, episodes.single { it.order == 1 }) }
                .filter { diagnosisEpisode ->
                    val episode = diagnosisEpisode.episode
                    episode.distantMetastasesDetectionStatus == MetastasesDetectionStatus.AT_START &&
                            episode.systemicTreatmentPlan?.treatment?.let{ it != Treatment.OTHER } == true &&
                            episode.surgeries.isEmpty() &&
                            episode.doesNotIncludeAdjuvantOrNeoadjuvantTreatment()
                }

            val patientsByTreatment = referencePop.groupBy { (_, episode) ->
                episode.systemicTreatmentPlan!!.treatment.treatmentGroup
            }
                .toList()
                .sortedByDescending { it.second.size }

            return PersonalizedDataInterpreter(patientsByTreatment)
        }
    }
}