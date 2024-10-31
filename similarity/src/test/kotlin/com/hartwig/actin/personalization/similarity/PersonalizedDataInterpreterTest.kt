package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.MetastasesDetectionStatus
import com.hartwig.actin.personalization.datamodel.Surgery
import com.hartwig.actin.personalization.datamodel.SurgeryType
import com.hartwig.actin.personalization.datamodel.Treatment
import com.hartwig.actin.personalization.datamodel.TreatmentGroup
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PersonalizedDataInterpreterTest {

    @Test
    fun `Should create interpreter with filtered and grouped patient records`() {
        val (fluourouracilDiagnosis, fluourouracilEpisode) = episodeWithTreatment(Treatment.FLUOROURACIL)
        val (capecitabineDiagnosis, capecitabineEpisode) = episodeWithTreatment(Treatment.CAPECITABINE)
        val (capoxDiagnosis, capoxEpisode) = episodeWithTreatment(Treatment.CAPOX)
        val (otherDiagnosis, episodeWithOtherTreatment) = episodeWithTreatment(Treatment.OTHER)
        val defaultDiagnosis = DIAGNOSIS

        val episodeWithoutSystemicPlan = episodeWithoutTreatment()

        val patients = listOf(
            recordWithEpisode(fluourouracilDiagnosis, fluourouracilEpisode),
            recordWithEpisode(fluourouracilDiagnosis, fluourouracilEpisode.copy(distantMetastasesDetectionStatus = MetastasesDetectionStatus.AT_PROGRESSION)),
            recordWithEpisode(otherDiagnosis, episodeWithOtherTreatment),
            recordWithEpisode(fluourouracilDiagnosis, fluourouracilEpisode.copy(systemicTreatmentPlan = null)),
            recordWithEpisode(fluourouracilDiagnosis, fluourouracilEpisode.copy(surgeries = listOf(Surgery(SurgeryType.NOS_OR_OTHER)))),
            recordWithEpisode(fluourouracilDiagnosis, fluourouracilEpisode.copy(hasHadPostSurgerySystemicChemotherapy = true)),
            recordWithEpisode(capecitabineDiagnosis, capecitabineEpisode),
            recordWithEpisode(capoxDiagnosis, capoxEpisode),
            recordWithEpisode(defaultDiagnosis, episodeWithoutSystemicPlan)
        )

        val interpreter = PersonalizedDataInterpreter.createFromReferencePatients(patients)
        assertThat(interpreter.patientsByTreatment).containsExactlyInAnyOrder(
            TreatmentGroup.CAPECITABINE_OR_FLUOROURACIL to listOf(
                fluourouracilDiagnosis to fluourouracilEpisode,
                capecitabineDiagnosis to capecitabineEpisode
            ),
            TreatmentGroup.CAPOX_OR_FOLFOX to listOf(
                capoxDiagnosis to capoxEpisode
            ),
            TreatmentGroup.NONE to listOf(
                defaultDiagnosis to episodeWithoutSystemicPlan,
                fluourouracilDiagnosis to fluourouracilEpisode.copy(systemicTreatmentPlan = null)
            ),
            TreatmentGroup.OTHER to listOf(
                otherDiagnosis to episodeWithOtherTreatment
            )
        )

    }
}
