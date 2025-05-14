package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.TestDatamodelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class WhoAssessmentsTest {

    @Test
    fun `Should have no WHO status upon empty list`() {
        assertThat(
            WhoAssessments.statusAtMetastaticDiagnosis(
                whoAssessments = emptyList(),
                daysBetweenPrimaryAndMetastaticDiagnosis = 0
            )
        ).isNull()
    }

    @Test
    fun `Should determine WHO status at metastatic diagnosis`() {
        val whoAssessments = listOf(
            TestDatamodelFactory.whoAssessment(daysSinceDiagnosis = 10, whoStatus = 0),
            TestDatamodelFactory.whoAssessment(daysSinceDiagnosis = 20, whoStatus = 1),
            TestDatamodelFactory.whoAssessment(daysSinceDiagnosis = 30, whoStatus = 2)
        )

        assertThat(
            WhoAssessments.statusAtMetastaticDiagnosis(
                whoAssessments,
                daysBetweenPrimaryAndMetastaticDiagnosis = 0
            )
        ).isNull()
        assertThat(
            WhoAssessments.statusAtMetastaticDiagnosis(
                whoAssessments,
                daysBetweenPrimaryAndMetastaticDiagnosis = 10
            )
        ).isEqualTo(0)
        assertThat(
            WhoAssessments.statusAtMetastaticDiagnosis(
                whoAssessments,
                daysBetweenPrimaryAndMetastaticDiagnosis = 25
            )
        ).isEqualTo(1)
        assertThat(
            WhoAssessments.statusAtMetastaticDiagnosis(
                whoAssessments,
                daysBetweenPrimaryAndMetastaticDiagnosis = 50
            )
        ).isEqualTo(2)
    }
}