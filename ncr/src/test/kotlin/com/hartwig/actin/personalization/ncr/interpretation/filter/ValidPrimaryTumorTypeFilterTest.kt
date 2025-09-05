package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ValidPrimaryTumorTypeFilterTest {

    private val filter = ValidPrimaryTumorTypeFilter(true)
    private val diagnosis = TestNcrRecordFactory.minimalDiagnosisRecord()
    private val followup = TestNcrRecordFactory.minimalFollowupRecord()

    @Test
    fun `Should return true for valid primary tumor type data`() {
        val records = listOf(
            diagnosis.copy(primaryDiagnosis = diagnosis.primaryDiagnosis.copy(morfCat = 1)),
            followup.copy(primaryDiagnosis = followup.primaryDiagnosis.copy(morfCat = null))
        )
        
        assertThat(filter.apply(records)).isTrue()
    }

    @Test
    fun `Should return false for invalid primary tumor type data`() {
        val records = listOf(
            diagnosis.copy(primaryDiagnosis = diagnosis.primaryDiagnosis.copy(morfCat = null)),
            followup.copy(primaryDiagnosis = followup.primaryDiagnosis.copy(morfCat = 1))
        )
        
        assertThat(filter.apply(records)).isFalse()
    }
}