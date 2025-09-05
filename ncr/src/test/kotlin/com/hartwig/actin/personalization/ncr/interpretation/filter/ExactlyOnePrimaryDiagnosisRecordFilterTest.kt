package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ExactlyOnePrimaryDiagnosisRecordFilterTest {

    private val filter = ExactlyOnePrimaryDiagnosisRecordFilter(true)
    private val minimalDiagnosis = TestNcrRecordFactory.minimalDiagnosisRecord()

    @Test
    fun `Should return true when there is exactly one diagnosis`() {
        val records = listOf(minimalDiagnosis)
        assertThat(filter.apply(records)).isTrue()
    }

    @Test
    fun `Should return false when there are zero or multiple diagnoses`() {
        val notDiagnosis = minimalDiagnosis.copy(identification = minimalDiagnosis.identification.copy(epis = "NOT DIA"))
        assertThat(filter.apply(listOf(notDiagnosis))).isFalse()
        
        assertThat(filter.apply(listOf(minimalDiagnosis, minimalDiagnosis.copy()))).isFalse()
    }
}