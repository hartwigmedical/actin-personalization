package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ValidAnalDistanceFilterTest {

    private val filter = ValidAnalDistanceFilter(true)
    private val diagnosis = TestNcrRecordFactory.minimalDiagnosisRecord()
    private val followup = TestNcrRecordFactory.minimalFollowupRecord()

    @Test
    fun `Should return true for valid anal distance data`() {
        val records = listOf(
            diagnosis,
            followup.copy(clinicalCharacteristics = followup.clinicalCharacteristics.copy(anusAfst = null))
        )

        assertThat(filter.apply(records)).isTrue()
    }

    @Test
    fun `Should return false for invalid anal distance data`() {
        val records = listOf(
            diagnosis,
            followup.copy(clinicalCharacteristics = followup.clinicalCharacteristics.copy(anusAfst = 1))
        )
        assertThat(filter.apply(records)).isFalse()
    }
}
