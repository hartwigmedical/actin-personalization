package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.Treatment
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

        val fluourouracilEpisode = patientWithTreatment(treatment = Treatment.FLUOROURACIL).episode
        val capecitabineEpisode = patientWithTreatment(treatment = Treatment.CAPECITABINE).episode
        val capoxEpisode = patientWithTreatment(treatment = Treatment.CAPOX).episode

        val patients = listOf(
            recordWithEpisode(fluourouracilEpisode),
            recordWithEpisode(fluourouracilEpisode.copy(distantMetastasesDetectionStatus = MetastasesDetectionStatus.AT_PROGRESSION)),
            recordWithEpisode(patientWithTreatment(treatment = Treatment.OTHER).episode),
            recordWithEpisode(patientWithTreatment().episode),
            recordWithEpisode(fluourouracilEpisode.copy(surgeries = listOf(Surgery(SurgeryType.NOS_OR_OTHER)))),
            recordWithEpisode(fluourouracilEpisode.copy(hasHadPostSurgerySystemicChemotherapy = true)),
            recordWithEpisode(capecitabineEpisode),
            recordWithEpisode(capoxEpisode)
        )

        val interpreter = PersonalizedDataInterpreter.createFromReferencePatients(patients)

        val expectedDiagnosisEpisodes = mapOf(
            TreatmentGroup.CAPECITABINE_OR_FLUOROURACIL to listOf(
                DiagnosisEpisode(DIAGNOSIS, fluourouracilEpisode),
                DiagnosisEpisode(DIAGNOSIS, capecitabineEpisode)
            ),
            TreatmentGroup.CAPOX_OR_FOLFOX to listOf(
                DiagnosisEpisode(DIAGNOSIS, capoxEpisode)
            ),
            TreatmentGroup.NONE to listOf(
                DiagnosisEpisode(DIAGNOSIS, patientWithTreatment().episode)
            )
        )
        assertThat(interpreter.patientsByTreatment).containsExactlyInAnyOrder(
            *expectedDiagnosisEpisodes.entries.map { it.toPair() }.toTypedArray()
        )
    }
}