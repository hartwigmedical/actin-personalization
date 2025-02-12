package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.DiagnosisEpisode
import com.hartwig.actin.personalization.datamodel.MetastasesDetectionStatus
import com.hartwig.actin.personalization.datamodel.Surgery
import com.hartwig.actin.personalization.datamodel.Treatment
import com.hartwig.actin.personalization.datamodel.TreatmentGroup
import com.hartwig.actin.personalization.datamodel.v2.treatment.SurgeryType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PersonalizedDataInterpreterTest {

    @Test
    fun `Should create interpreter with filtered and grouped patient records`() {
        val fluorouracilEpisode = patientWithTreatment(treatment = Treatment.FLUOROURACIL).episode
        val capecitabineEpisode = patientWithTreatment(treatment = Treatment.CAPECITABINE).episode
        val capoxEpisode = patientWithTreatment(treatment = Treatment.CAPOX).episode

        val patients = listOf(
            recordWithEpisode(fluorouracilEpisode),
            recordWithEpisode(fluorouracilEpisode.copy(distantMetastasesDetectionStatus = MetastasesDetectionStatus.AT_PROGRESSION)),
            recordWithEpisode(patientWithTreatment(treatment = Treatment.OTHER).episode),
            recordWithEpisode(fluorouracilEpisode.copy(systemicTreatmentPlan = null)),
            recordWithEpisode(fluorouracilEpisode.copy(surgeries = listOf(Surgery(SurgeryType.NOS_OR_OTHER)))),
            recordWithEpisode(fluorouracilEpisode.copy(hasHadPostSurgerySystemicChemotherapy = true)),
            recordWithEpisode(capecitabineEpisode),
            recordWithEpisode(capoxEpisode)
        )

        val interpreter = PersonalizedDataInterpreter.createFromReferencePatients(patients)

        val expectedDiagnosisEpisodes = mapOf(
            TreatmentGroup.CAPECITABINE_OR_FLUOROURACIL to listOf(
                DiagnosisEpisode(DIAGNOSIS, fluorouracilEpisode),
                DiagnosisEpisode(DIAGNOSIS, capecitabineEpisode)
            ),
            TreatmentGroup.CAPOX_OR_FOLFOX to listOf(
                DiagnosisEpisode(DIAGNOSIS, capoxEpisode)
            )
        )

        assertThat(interpreter.patientsByTreatment).containsExactlyInAnyOrder(
            *expectedDiagnosisEpisodes.entries.map { it.toPair() }.toTypedArray()
        )
    }
}