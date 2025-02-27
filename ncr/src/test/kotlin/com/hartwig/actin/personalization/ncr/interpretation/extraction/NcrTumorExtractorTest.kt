package com.hartwig.actin.personalization.ncr.interpretation.extraction

import com.hartwig.actin.personalization.datamodel.assessment.AsaAssessment
import com.hartwig.actin.personalization.datamodel.assessment.AsaClassification
import com.hartwig.actin.personalization.datamodel.assessment.WhoAssessment
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

        assertThat(tumor.whoAssessments).isEmpty()
        assertThat(tumor.asaAssessments).isEmpty()
    }

    @Test
    fun `Should extract tumor from proper tumor NCR records`() {
        val tumor = NcrTumorExtractor.extractTumor(TestNcrRecordFactory.properTumorRecords())

        assertThat(tumor.diagnosisYear).isEqualTo(2020)
        assertThat(tumor.ageAtDiagnosis).isEqualTo(75)

        assertThat(tumor.latestSurvivalStatus.daysSinceDiagnosis).isEqualTo(563)
        assertThat(tumor.latestSurvivalStatus.isAlive).isTrue()

        assertThat(tumor.whoAssessments).containsExactly(
            WhoAssessment(daysSinceDiagnosis = 0, whoStatus = 1),
            WhoAssessment(daysSinceDiagnosis = 50, whoStatus = 1),
            WhoAssessment(daysSinceDiagnosis = 100, whoStatus = 2)
        )
        assertThat(tumor.asaAssessments).containsExactly(
            AsaAssessment(daysSinceDiagnosis = 0, classification = AsaClassification.V),
            AsaAssessment(daysSinceDiagnosis = 50, classification = AsaClassification.V),
            AsaAssessment(daysSinceDiagnosis = 100, classification = AsaClassification.VI)
        )
    }
}
