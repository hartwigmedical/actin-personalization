package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.DistantMetastasesStatus
import com.hartwig.actin.personalization.datamodel.Surgery
import com.hartwig.actin.personalization.datamodel.SurgeryType
import com.hartwig.actin.personalization.datamodel.Treatment
import com.hartwig.actin.personalization.datamodel.TreatmentGroup
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PersonalizedDataInterpreterTest {

    @Test
    fun `Should create interpreter with filtered and grouped patient records`() {
        val fluourouracilEpisode = episodeWithTreatment(Treatment.FLUOROURACIL)
        val capecitabineEpisode = episodeWithTreatment(Treatment.CAPECITABINE)
        val capoxEpisode = episodeWithTreatment(Treatment.CAPOX)
        val patients = listOf(
            recordWithEpisode(fluourouracilEpisode),
            recordWithEpisode(fluourouracilEpisode.copy(distantMetastasesStatus = DistantMetastasesStatus.AT_PROGRESSION)),
            recordWithEpisode(episodeWithTreatment(Treatment.OTHER)),
            recordWithEpisode(fluourouracilEpisode.copy(systemicTreatmentPlan = null)),
            recordWithEpisode(fluourouracilEpisode.copy(surgeries = listOf(Surgery(SurgeryType.NOS_OR_OTHER)))),
            recordWithEpisode(fluourouracilEpisode.copy(hasHadPostSurgerySystemicChemotherapy = true)),
            recordWithEpisode(capecitabineEpisode),
            recordWithEpisode(capoxEpisode),
        )

        val interpreter = PersonalizedDataInterpreter.createFromPatientRecords(patients)
        assertThat(interpreter.patientsByTreatment).containsExactly(
            TreatmentGroup.CAPECITABINE_OR_FLUOROURACIL to listOf(DIAGNOSIS to fluourouracilEpisode, DIAGNOSIS to capecitabineEpisode),
            TreatmentGroup.CAPOX_OR_FOLFOX to listOf(DIAGNOSIS to capoxEpisode)
        )
    }
}