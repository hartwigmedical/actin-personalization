package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NcrTumorExtractorTest {

    @Test
    fun `Should extract tumor from minimal diagnosis NCR record`() {
        val tumor = NcrTumorExtractor.extractTumor(listOf(TestNcrRecordFactory.minimalDiagnosisRecord()))
        assertThat(tumor.diagnosisYear).isEqualTo(2020)
        assertThat(tumor.ageAtDiagnosis).isEqualTo(75)
        assertThat(tumor.latestSurvivalStatus.daysSinceDiagnosis).isEqualTo(563)
        assertThat(tumor.latestSurvivalStatus.isAlive).isTrue()
    }

    @Test
    fun `Should extract tumor from proper diagnosis and followup NCR records`() {
        val records = listOf(
            TestNcrRecordFactory.properDiagnosisRecord(),
            TestNcrRecordFactory.properFollowupRecord1(),
            TestNcrRecordFactory.properFollowupRecord2()
        )

        val tumor = NcrTumorExtractor.extractTumor(records)
        assertThat(tumor.diagnosisYear).isEqualTo(2020)
        assertThat(tumor.ageAtDiagnosis).isEqualTo(75)
        assertThat(tumor.latestSurvivalStatus.daysSinceDiagnosis).isEqualTo(563)
        assertThat(tumor.latestSurvivalStatus.isAlive).isTrue()
    }
}
