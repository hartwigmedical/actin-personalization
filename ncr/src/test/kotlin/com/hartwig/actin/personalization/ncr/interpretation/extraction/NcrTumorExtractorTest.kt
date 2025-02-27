package com.hartwig.actin.personalization.ncr.interpretation.extraction

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NcrTumorExtractorTest {

    @Test
    fun `Should extract tumor from minimal diagnosis NCR record`() {
        val tumor = NcrTumorExtractor.extractTumor(TestNcrRecordFactory.minimalTumorRecords())

        assertThat(tumor.diagnosisYear).isEqualTo(2020)
        assertThat(tumor.ageAtDiagnosis).isEqualTo(75)
        assertThat(tumor.latestSurvivalStatus.daysSinceDiagnosis).isEqualTo(563)
        assertThat(tumor.latestSurvivalStatus.isAlive).isTrue()
    }

    @Test
    fun `Should extract tumor from proper tumor NCR records`() {
        val tumor = NcrTumorExtractor.extractTumor(TestNcrRecordFactory.properTumorRecords())

        assertThat(tumor.diagnosisYear).isEqualTo(2020)
        assertThat(tumor.ageAtDiagnosis).isEqualTo(75)
        assertThat(tumor.latestSurvivalStatus.daysSinceDiagnosis).isEqualTo(563)
        assertThat(tumor.latestSurvivalStatus.isAlive).isTrue()
    }
}
