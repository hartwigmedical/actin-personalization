package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MaxOneMetastaticRecordFilterTest {

    private val filter = MaxOneMetastaticRecordFilter(true)
    private val diagnosis = TestNcrRecordFactory.minimalDiagnosisRecord()

    @Test
    fun `Should return true for zero metastatic records`() {
        val records = listOf(diagnosis.copy(identification = diagnosis.identification.copy(metaEpis = 0)))
        assertThat(filter.apply(records)).isTrue()
    }

    @Test
    fun `Should return true for one record with metastatic detection`() {
        val records = listOf(diagnosis.copy(identification = diagnosis.identification.copy(metaEpis = 1)))
        assertThat(filter.apply(records)).isTrue()
    }

    @Test
    fun `Should return false for more than one record with metastatic detection`() {
        val records = listOf(
            diagnosis.copy(identification = diagnosis.identification.copy(metaEpis = 1)),
            diagnosis.copy(identification = diagnosis.identification.copy(metaEpis = 2))
        )
        assertThat(filter.apply(records)).isFalse()
    }
}