package com.hartwig.actin.personalization.ncr.util

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class NcrFunctionsTest {

    @Test
    fun `Should extract trivially a single diagnosis record from a single element set of records`() {
        val diagnosis = TestNcrRecordFactory.minimalDiagnosisRecord()

        assertThat(NcrFunctions.diagnosisRecord(listOf(diagnosis))).isEqualTo(diagnosis)
    }

    @Test
    fun `Should extract diagnosis record for multiple records`() {
        val diagnosis = TestNcrRecordFactory.minimalDiagnosisRecord()
        val followup1 = TestNcrRecordFactory.minimalFollowupRecord()
        val followup2 = TestNcrRecordFactory.minimalFollowupRecord()

        assertThat(NcrFunctions.diagnosisRecord(listOf(diagnosis, followup1, followup2))).isEqualTo(diagnosis)
    }

    @Test
    fun `Should throw exception upon multiple diagnoses`() {
        val diagnosis1 = TestNcrRecordFactory.minimalDiagnosisRecord()
        val diagnosis2 = TestNcrRecordFactory.minimalDiagnosisRecord()

        assertThrows<Exception> { NcrFunctions.diagnosisRecord(listOf(diagnosis1, diagnosis2)) }
    }

    @Test
    fun `Should throw exception upon zero diagnoses`() {
        val followup1 = TestNcrRecordFactory.minimalFollowupRecord()
        val followup2 = TestNcrRecordFactory.minimalFollowupRecord()

        assertThrows<Exception> { NcrFunctions.diagnosisRecord(listOf(followup1, followup2)) }
    }
}