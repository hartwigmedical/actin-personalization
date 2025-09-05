package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NoMolecularDataForFollowupFilterTest {
    
    private val filter = NoMolecularDataForFollowupFilter(true)
    private val followup = TestNcrRecordFactory.minimalFollowupRecord()

    @Test
    fun `Should return false for non-empty molecular data in followup`() {
        val records = listOf(followup.copy(
            molecularCharacteristics = followup.molecularCharacteristics.copy(brafMut = 1, rasMut = 1, msiStat = 1)
        ))
        
        assertThat(filter.apply(records)).isFalse()
    }

    @Test
    fun `Should return true for empty molecular data in followup`() {
        assertThat(filter.apply(listOf(followup))).isTrue()
    }
}
