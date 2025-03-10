package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.TreatmentGroup
import com.hartwig.actin.personalization.datamodel.DiagnosisEpisode
import com.hartwig.actin.personalization.datamodel.LocationGroup
import com.hartwig.actin.personalization.datamodel.ReferencePatient
import com.hartwig.actin.personalization.datamodel.MetastasesDetectionStatus
import com.hartwig.actin.personalization.datamodel.StageTnm
import com.hartwig.actin.personalization.datamodel.TnmM
import com.hartwig.actin.personalization.datamodel.Treatment
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
            return createFromReferencePatients(patients)
        }

        fun createFromReferencePatients(patients: List<ReferencePatient>): PersonalizedDataInterpreter {
            val referencePop = patients
                .flatMap(ReferencePatient::tumorEntries)
                .map { (diagnosis, episodes) -> DiagnosisEpisode(diagnosis, episodes.single { it.order == 1 }) }
                .filter { diagnosisEpisode ->
                    val episode = diagnosisEpisode.episode
                    val tnmM1 = setOf(TnmM.M1, TnmM.M1A, TnmM.M1B, TnmM.M1C)
                    val stageTnmIV = setOf(StageTnm.IV, StageTnm.IVA, StageTnm.IVB, StageTnm.IVC)
                    episode.distantMetastasesDetectionStatus == MetastasesDetectionStatus.AT_START &&
                            (episode.tnmCM in tnmM1 || episode.tnmPM in tnmM1 || episode.stageTNM in stageTnmIV) &&
                            !episode.hasHadPreSurgerySystemicChemotherapy &&
                            !episode.hasHadPostSurgerySystemicChemotherapy &&
                            !episode.hasHadPreSurgerySystemicTargetedTherapy &&
                            !episode.hasHadPostSurgerySystemicTargetedTherapy &&
                            episode.surgeries.isEmpty() &&
                            episode.doesNotIncludeAdjuvantOrNeoadjuvantTreatment() &&
                            episode.gastroenterologyResections.isEmpty() &&
                            episode.metastasesSurgeries.isEmpty() &&
                            episode.radiotherapies.isEmpty() &&
                            episode.metastasesRadiotherapies.isEmpty() &&
                            !episode.hasHadHipecTreatment &&
                            (episode.hasReceivedTumorDirectedTreatment || episode.systemicTreatmentPlan != null)
                            episode.systemicTreatmentPlan?.treatment?.let{ it != Treatment.OTHER } == true
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