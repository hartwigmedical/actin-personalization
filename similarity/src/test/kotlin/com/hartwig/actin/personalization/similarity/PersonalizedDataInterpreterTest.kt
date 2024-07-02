package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.DistantMetastasesStatus
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.PatientRecord
import com.hartwig.actin.personalization.datamodel.Surgery
import com.hartwig.actin.personalization.datamodel.SurgeryType
import com.hartwig.actin.personalization.datamodel.SystemicTreatmentPlan
import com.hartwig.actin.personalization.datamodel.Treatment
import com.hartwig.actin.personalization.datamodel.TreatmentGroup
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PersonalizedDataInterpreterTest {

    @Test
    fun `Should create interpreter with filtered and grouped patient records`() {
        val capecitabineEpisode = episodeWithTreatment(Treatment.CAPECITABINE)
        val capoxEpisode = episodeWithTreatment(Treatment.CAPOX)
        val patients = listOf(
            recordWithEpisode(ELIGIBLE_EPISODE),
            recordWithEpisode(ELIGIBLE_EPISODE.copy(distantMetastasesStatus = DistantMetastasesStatus.AT_PROGRESSION)),
            recordWithEpisode(episodeWithTreatment(Treatment.OTHER)),
            recordWithEpisode(ELIGIBLE_EPISODE.copy(systemicTreatmentPlan = null)),
            recordWithEpisode(ELIGIBLE_EPISODE.copy(surgeries = listOf(Surgery(SurgeryType.NOS_OR_OTHER)))),
            recordWithEpisode(ELIGIBLE_EPISODE.copy(hasHadPostSurgerySystemicChemotherapy = true)),
            recordWithEpisode(capecitabineEpisode),
            recordWithEpisode(capoxEpisode),
        )

        val interpreter = PersonalizedDataInterpreter.createFromPatientRecords(patients)
        assertThat(interpreter.patientsByTreatment.map { it.toPair() }).containsExactly(
            TreatmentGroup.CAPECITABINE_OR_FLUOROURACIL to listOf(DIAGNOSIS to ELIGIBLE_EPISODE, DIAGNOSIS to capecitabineEpisode),
            TreatmentGroup.CAPOX_OR_FOLFOX to listOf(DIAGNOSIS to capoxEpisode)
        )
    }

    companion object {
        private val ELIGIBLE_EPISODE = episodeWithTreatment(Treatment.FLUOROURACIL)

        private fun recordWithEpisode(episode: Episode): PatientRecord {
            return PATIENT_RECORD.copy(tumorEntries = PATIENT_RECORD.tumorEntries.map { it.copy(episodes = listOf(episode)) })
        }

        private fun episodeWithTreatment(treatment: Treatment): Episode {
            return EPISODE.copy(
                systemicTreatmentPlan = SystemicTreatmentPlan(treatment = treatment, systemicTreatmentSchemes = emptyList())
            )
        }
    }
}