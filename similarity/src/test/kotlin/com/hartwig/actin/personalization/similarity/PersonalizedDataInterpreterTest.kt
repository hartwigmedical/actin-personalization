package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.TumorEntry
import com.hartwig.actin.personalization.datamodel.Treatment
import com.hartwig.actin.personalization.datamodel.ReferencePatient
import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.datamodel.DiagnosisEpisode
import com.hartwig.actin.personalization.datamodel.TreatmentGroup
import com.hartwig.actin.personalization.datamodel.MetastasesDetectionStatus
import com.hartwig.actin.personalization.datamodel.Surgery
import com.hartwig.actin.personalization.datamodel.SurgeryType

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PersonalizedDataInterpreterTest {

    @Test
    fun `Should create interpreter with filtered and grouped patient records`() {
        val ncrId = 1
        val sex = Sex.MALE
        val isAlive = true

        val fluourouracilTumorEntry = TumorEntry(diagnosis = DIAGNOSIS, episodes = listOf(patientWithTreatment(Treatment.FLUOROURACIL).episode))
        val capecitabineTumorEntry = TumorEntry(diagnosis = DIAGNOSIS, episodes = listOf(patientWithTreatment(Treatment.CAPECITABINE).episode))
        val capoxTumorEntry = TumorEntry(diagnosis = DIAGNOSIS, episodes = listOf(patientWithTreatment(Treatment.CAPOX).episode))
        val otherTumorEntry = TumorEntry(diagnosis = DIAGNOSIS, episodes = listOf(patientWithTreatment(Treatment.OTHER).episode))
        val noTreatmentTumorEntry = TumorEntry(diagnosis = DIAGNOSIS, episodes = listOf(patientWithoutTreatment()))

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

        val fluourouracilDiagnosisEpisode = DiagnosisEpisode(DIAGNOSIS, fluourouracilTumorEntry.episodes.first())
        val capecitabineDiagnosisEpisode = DiagnosisEpisode(DIAGNOSIS, capecitabineTumorEntry.episodes.first())
        val capoxDiagnosisEpisode = DiagnosisEpisode(DIAGNOSIS, capoxTumorEntry.episodes.first())
        val noTreatmentDiagnosisEpisode = DiagnosisEpisode(DIAGNOSIS, noTreatmentTumorEntry.episodes.first())

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
            )
        )
    }
}
