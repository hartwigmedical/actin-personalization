package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.TestDatamodelFactory
import com.hartwig.actin.personalization.datamodel.assessment.AsaClassification
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AsaAssessmentsTest {

    @Test
    fun `Should have no ASA classification upon empty list`() {
        assertThat(
            AsaAssessments.classificationAtMetastaticDiagnosis(
                asaAssessments = emptyList(),
                daysBetweenPrimaryAndMetastaticDiagnosis = 0
            )
        ).isNull()
    }

    @Test
    fun `Should determine ASA classification at metastatic diagnosis`() {
        val asaAssessments = listOf(
            TestDatamodelFactory.asaAssessment(daysSinceDiagnosis = 10, classification = AsaClassification.I),
            TestDatamodelFactory.asaAssessment(daysSinceDiagnosis = 20, classification = AsaClassification.II),
            TestDatamodelFactory.asaAssessment(daysSinceDiagnosis = 30, classification = AsaClassification.III)
        )

        assertThat(
            AsaAssessments.classificationAtMetastaticDiagnosis(
                asaAssessments,
                daysBetweenPrimaryAndMetastaticDiagnosis = 0
            )
        ).isNull()
        assertThat(
            AsaAssessments.classificationAtMetastaticDiagnosis(
                asaAssessments,
                daysBetweenPrimaryAndMetastaticDiagnosis = 10
            )
        ).isEqualTo(AsaClassification.I)
        assertThat(
            AsaAssessments.classificationAtMetastaticDiagnosis(
                asaAssessments,
                daysBetweenPrimaryAndMetastaticDiagnosis = 25
            )
        ).isEqualTo(AsaClassification.II)
        assertThat(
            AsaAssessments.classificationAtMetastaticDiagnosis(
                asaAssessments,
                daysBetweenPrimaryAndMetastaticDiagnosis = 50
            )
        ).isEqualTo(AsaClassification.III)
    }
}