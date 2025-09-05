package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ConsistentDoubleTumorDataFilterTest {

    private val filter = ConsistentDoubleTumorDataFilter(true)
    private val diagnosis = TestNcrRecordFactory.minimalDiagnosisRecord()
    private val followup = TestNcrRecordFactory.minimalFollowupRecord()

    @Test
    fun `Should return true for valid double tumor data`() {
        val records = listOf(
            diagnosis.copy(clinicalCharacteristics = diagnosis.clinicalCharacteristics.copy(dubbeltum = 1)),
            followup.copy(clinicalCharacteristics = followup.clinicalCharacteristics.copy(dubbeltum = 0))
        )
        
        assertThat(filter.apply(records)).isTrue()
    }

    @Test
    fun `Should return false for no double tumor data in diagnosis record`() {
        val records = listOf(
            diagnosis.copy(clinicalCharacteristics = diagnosis.clinicalCharacteristics.copy(dubbeltum = null)),
            followup.copy(clinicalCharacteristics = followup.clinicalCharacteristics.copy(dubbeltum = null))
        )
        
        assertThat(filter.apply(records)).isFalse()
    }

    @Test
    fun `Should return false for double tumor data present in followup record`() {
        val records = listOf(
            diagnosis.copy(clinicalCharacteristics = diagnosis.clinicalCharacteristics.copy(dubbeltum = 1)),
            followup.copy(clinicalCharacteristics = followup.clinicalCharacteristics.copy(dubbeltum = 1))
        )
        
        assertThat(filter.apply(records)).isFalse()
    }
}