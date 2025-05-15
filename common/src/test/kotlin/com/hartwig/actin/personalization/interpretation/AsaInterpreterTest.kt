package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.TestDatamodelFactory
import com.hartwig.actin.personalization.datamodel.assessment.AsaClassification
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AsaInterpreterTest {

    @Test
    fun `Should have no ASA classification upon empty list`() {
        val interpreter = AsaInterpreter(emptyList())

        assertThat(interpreter.mostRecentClassificationPriorTo(maxDaysSinceDiagnosis = 0)).isNull()
    }

    @Test
    fun `Should determine ASA classification correctly for multiple intervals`() {
        val asaAssessments = listOf(
            TestDatamodelFactory.asaAssessment(daysSinceDiagnosis = 10, classification = AsaClassification.I),
            TestDatamodelFactory.asaAssessment(daysSinceDiagnosis = 20, classification = AsaClassification.II),
            TestDatamodelFactory.asaAssessment(daysSinceDiagnosis = 30, classification = AsaClassification.III)
        )

        val interpreter = AsaInterpreter(asaAssessments)

        assertThat(interpreter.mostRecentClassificationPriorTo(maxDaysSinceDiagnosis = 0)).isNull()
        assertThat(interpreter.mostRecentClassificationPriorTo(maxDaysSinceDiagnosis = 10)).isEqualTo(AsaClassification.I)
        assertThat(interpreter.mostRecentClassificationPriorTo(maxDaysSinceDiagnosis = 25)).isEqualTo(AsaClassification.II)
        assertThat(interpreter.mostRecentClassificationPriorTo(maxDaysSinceDiagnosis = 50)).isEqualTo(AsaClassification.III)
    }
}