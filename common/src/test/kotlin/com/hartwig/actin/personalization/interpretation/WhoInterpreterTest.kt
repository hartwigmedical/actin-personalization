package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.TestDatamodelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class WhoInterpreterTest {

    @Test
    fun `Should have no WHO status upon empty list`() {
        val interpreter = WhoInterpreter(emptyList())
        
        assertThat(interpreter.mostRecentStatusPriorTo(maxDaysSinceDiagnosis = 0)).isNull()
    }

    @Test
    fun `Should determine WHO status correctly for multiple intervals`() {
        val whoAssessments = listOf(
            TestDatamodelFactory.whoAssessment(daysSinceDiagnosis = 10, whoStatus = 0),
            TestDatamodelFactory.whoAssessment(daysSinceDiagnosis = 20, whoStatus = 1),
            TestDatamodelFactory.whoAssessment(daysSinceDiagnosis = 30, whoStatus = 2)
        )

        val interpreter = WhoInterpreter(whoAssessments)

        assertThat(interpreter.mostRecentStatusPriorTo(maxDaysSinceDiagnosis = 0)).isNull()
        assertThat(interpreter.mostRecentStatusPriorTo(maxDaysSinceDiagnosis = 10)).isEqualTo(0)
        assertThat(interpreter.mostRecentStatusPriorTo(maxDaysSinceDiagnosis = 25)).isEqualTo(1)
        assertThat(interpreter.mostRecentStatusPriorTo(maxDaysSinceDiagnosis = 50)).isEqualTo(2)
    }
}