package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ConsistentSexFilterTest {

    private val filter = ConsistentSexFilter(true)
    private val minimal = TestNcrRecordFactory.minimalDiagnosisRecord()

    @Test
    fun `Should return true when all records have identical sex`() {
        val record1 = minimal.copy(
            patientCharacteristics = minimal.patientCharacteristics.copy(gesl = 1)
        )
        val record2 = record1.copy()
        val records = listOf(record1, record2)
        
        assertThat(filter.apply(records)).isTrue()
    }

    @Test
    fun `Should return false when records have different sex`() {
        val record1 = minimal.copy(
            patientCharacteristics = minimal.patientCharacteristics.copy(gesl = 1)
        )
        val record2 = record1.copy(patientCharacteristics = record1.patientCharacteristics.copy(gesl = 2))
        val records = listOf(record1, record2)
        
        assertThat(filter.apply(records)).isFalse()
    }
}