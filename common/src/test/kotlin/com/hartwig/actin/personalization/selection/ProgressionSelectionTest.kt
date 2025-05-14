package com.hartwig.actin.personalization.selection

import com.hartwig.actin.personalization.datamodel.TestDatamodelFactory
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureType
import com.hartwig.actin.personalization.datamodel.treatment.Treatment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ProgressionSelectionTest {

    @Test
    fun `Should return null when no progression measures present`() {
        assertThat(
            ProgressionSelection.firstProgressionAfterSystemicTreatmentStart(
                TestDatamodelFactory.entry(systemicTreatment = Treatment.CAPOX)
            )
        ).isNull()
    }

    @Test
    fun `Should return null when progression measures are before treatment start`() {
        val treatmentEpisode =
            TestDatamodelFactory.treatmentEpisode(
                systemicTreatment = Treatment.CAPOX,
                daysBetweenDiagnosisAndSystemicTreatmentStart = 10,
                hasProgressionEvent = true,
                daysBetweenDiagnosisAndProgression = 8
            )

        assertThat(
            ProgressionSelection.firstProgressionAfterSystemicTreatmentStart(
                TestDatamodelFactory.entry(treatmentEpisode = treatmentEpisode)
            )
        ).isNull()
    }

    @Test
    fun `Should return first progression measure when multiple after treatment start`() {
        val treatmentEpisodeWithMultipleProgression =
            TestDatamodelFactory.treatmentEpisode(
                systemicTreatment = Treatment.CAPOX,
                daysBetweenDiagnosisAndSystemicTreatmentStart = 10
            ).copy(
                progressionMeasures = listOf(
                    TestDatamodelFactory.progressionMeasure(daysSinceDiagnosis = 15),
                    TestDatamodelFactory.progressionMeasure(daysSinceDiagnosis = 18),
                    TestDatamodelFactory.progressionMeasure(daysSinceDiagnosis = 12),
                    TestDatamodelFactory.progressionMeasure(daysSinceDiagnosis = 11, type = ProgressionMeasureType.CENSOR),
                    TestDatamodelFactory.progressionMeasure(daysSinceDiagnosis = 8)
                )
            )

        assertThat(
            ProgressionSelection.firstProgressionAfterSystemicTreatmentStart(
                TestDatamodelFactory.entry(treatmentEpisode = treatmentEpisodeWithMultipleProgression)
            )?.daysSinceDiagnosis
        ).isEqualTo(12)
    }
}