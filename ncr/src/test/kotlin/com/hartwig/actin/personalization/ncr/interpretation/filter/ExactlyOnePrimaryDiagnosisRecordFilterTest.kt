package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ExactlyOnePrimaryDiagnosisRecordFilterTest {

    private val filter = ExactlyOnePrimaryDiagnosisRecordFilter(true)
    private val diagnosis = TestNcrRecordFactory.minimalDiagnosisRecord()

    @Test
    fun `Should return true when there is exactly one diagnosis`() {
        val records = listOf(diagnosis)
        assertThat(filter.apply(records)).isTrue()
    }

    @Test
    fun `Should return false when there are zero or multiple diagnoses`() {
        val notDiagnosis = diagnosis.copy(identification = diagnosis.identification.copy(epis = "NOT DIA"))
        assertThat(filter.apply(listOf(notDiagnosis))).isFalse()
        
        assertThat(filter.apply(listOf(diagnosis, diagnosis.copy()))).isFalse()
    }
}