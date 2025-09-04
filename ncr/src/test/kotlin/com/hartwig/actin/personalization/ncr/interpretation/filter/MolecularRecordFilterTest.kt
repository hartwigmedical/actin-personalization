package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MolecularRecordFilterTest {
    
    private val filter = MolecularRecordFilter(true)

    @Test
    fun `Should return false for non-empty molecular data in followup`() {
        val records = listOf(TestNcrRecordFactory.minimalFollowupRecord().copy(
            molecularCharacteristics = TestNcrRecordFactory.minimalFollowupRecord().molecularCharacteristics.copy(
                brafMut = 1, rasMut = 1, msiStat = 1
            )
        ))
        assertThat(filter.hasNoMolecularDataForFollowup(records)).isFalse()
    }

    @Test
    fun `Should return true for empty molecular data in followup`() {
        val records = listOf(TestNcrRecordFactory.minimalFollowupRecord())
        assertThat(filter.hasNoMolecularDataForFollowup(records)).isTrue()
    }
}
