package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PersonalizedDataInterpreterTest {

    @Test
    fun `Should create interpreter with filtered and grouped patient records`() {
        // Example values for required fields in ReferencePatient
        val ncrId = 1
        val sex = Sex.MALE
        val isAlive = true

        // Create TumorEntry instances for each Diagnosis and Episode pair
        val fluourouracilTumorEntry = TumorEntry(diagnosis = DIAGNOSIS, episodes = listOf(patientWithTreatment(Treatment.FLUOROURACIL).episode))
        val capecitabineTumorEntry = TumorEntry(diagnosis = DIAGNOSIS, episodes = listOf(patientWithTreatment(Treatment.CAPECITABINE).episode))
        val capoxTumorEntry = TumorEntry(diagnosis = DIAGNOSIS, episodes = listOf(patientWithTreatment(Treatment.CAPOX).episode))
        val otherTumorEntry = TumorEntry(diagnosis = DIAGNOSIS, episodes = listOf(patientWithTreatment(Treatment.OTHER).episode))
        val noTreatmentTumorEntry = TumorEntry(diagnosis = DIAGNOSIS, episodes = listOf(patientWithoutTreatment()))

        // Create ReferencePatient instances with TumorEntry lists
        val patients = listOf(
            ReferencePatient(ncrId, sex, isAlive, listOf(fluourouracilTumorEntry)),
            ReferencePatient(ncrId, sex, isAlive, listOf(fluourouracilTumorEntry.copy(episodes = listOf(fluourouracilTumorEntry.episodes.first().copy(distantMetastasesDetectionStatus = MetastasesDetectionStatus.AT_PROGRESSION))))),
            ReferencePatient(ncrId, sex, isAlive, listOf(otherTumorEntry)),
            ReferencePatient(ncrId, sex, isAlive, listOf(fluourouracilTumorEntry.copy(episodes = listOf(fluourouracilTumorEntry.episodes.first().copy(surgeries = listOf(Surgery(SurgeryType.NOS_OR_OTHER))))))),
            ReferencePatient(ncrId, sex, isAlive, listOf(fluourouracilTumorEntry.copy(episodes = listOf(fluourouracilTumorEntry.episodes.first().copy(hasHadPostSurgerySystemicChemotherapy = true))))),
            ReferencePatient(ncrId, sex, isAlive, listOf(capecitabineTumorEntry)),
            ReferencePatient(ncrId, sex, isAlive, listOf(capoxTumorEntry)),
            ReferencePatient(ncrId, sex, isAlive, listOf(noTreatmentTumorEntry))
        )

        val interpreter = PersonalizedDataInterpreter.createFromReferencePatients(patients)

        // Create expected DiagnosisEpisodeTreatment instances for assertions
        val fluourouracilDiagnosisEpisode = DiagnosisEpisodeTreatment(DIAGNOSIS, fluourouracilTumorEntry.episodes.first(), fluourouracilTumorEntry.episodes.first().systemicTreatmentPlan)
        val capecitabineDiagnosisEpisode = DiagnosisEpisodeTreatment(DIAGNOSIS, capecitabineTumorEntry.episodes.first(), capecitabineTumorEntry.episodes.first().systemicTreatmentPlan)
        val capoxDiagnosisEpisode = DiagnosisEpisodeTreatment(DIAGNOSIS, capoxTumorEntry.episodes.first(), capoxTumorEntry.episodes.first().systemicTreatmentPlan)
        val otherDiagnosisEpisode = DiagnosisEpisodeTreatment(DIAGNOSIS, otherTumorEntry.episodes.first(), otherTumorEntry.episodes.first().systemicTreatmentPlan)
        val noTreatmentDiagnosisEpisode = DiagnosisEpisodeTreatment(DIAGNOSIS, noTreatmentTumorEntry.episodes.first(), null)

        assertThat(interpreter.patientsByTreatment).containsExactlyInAnyOrder(
            TreatmentGroup.CAPECITABINE_OR_FLUOROURACIL to listOf(
                fluourouracilDiagnosisEpisode,
                capecitabineDiagnosisEpisode
            ),
            TreatmentGroup.CAPOX_OR_FOLFOX to listOf(
                capoxDiagnosisEpisode
            ),
            TreatmentGroup.NONE to listOf(
                noTreatmentDiagnosisEpisode
            ),
            TreatmentGroup.OTHER to listOf(
                otherDiagnosisEpisode
            )
        )
    }
}
