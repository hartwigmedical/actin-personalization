package com.hartwig.actin.personalization.ncr.interpretation

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class NcrFunctionsTest {

    @Test
    fun `Should extract diagnosis and metastatic records from a single element set of records`() {
        val record = TestNcrRecordFactory.minimalEntryRecords()[0]

        assertThat(NcrFunctions.diagnosisRecord(listOf(record))).isEqualTo(record)
        assertThat(NcrFunctions.metastaticRecord(listOf(record))).isEqualTo(record)
    }

    @Test
    fun `Should extract diagnosis and metastatic record for multiple records`() {
        val diagnosis = TestNcrRecordFactory.minimalDiagnosisRecord()
        val followup = TestNcrRecordFactory.minimalFollowupRecord()

        assertThat(NcrFunctions.diagnosisRecord(listOf(diagnosis, followup))).isEqualTo(diagnosis)
        assertThat(NcrFunctions.metastaticRecord(listOf(diagnosis, followup))).isEqualTo(followup)
    }

    @Test
    fun `Should throw exception upon multiple diagnosis records`() {
        val diagnosis1 = TestNcrRecordFactory.minimalDiagnosisRecord()
        val diagnosis2 = TestNcrRecordFactory.minimalDiagnosisRecord()

        assertThrows<Exception> { NcrFunctions.diagnosisRecord(listOf(diagnosis1, diagnosis2)) }
    }

    @Test
    fun `Should throw exception upon missing metastatic record`() {
        val diagnosis1 = TestNcrRecordFactory.minimalDiagnosisRecord()
        val diagnosis2 = TestNcrRecordFactory.minimalDiagnosisRecord()

        assertThrows<Exception> { NcrFunctions.metastaticRecord(listOf(diagnosis1, diagnosis2)) }
    }

    @Test
    fun `Should throw exception upon zero diagnosis records`() {
        val followup1 = TestNcrRecordFactory.minimalFollowupRecord()
        val followup2 = TestNcrRecordFactory.minimalFollowupRecord()

        assertThrows<Exception> { NcrFunctions.diagnosisRecord(listOf(followup1, followup2)) }
    }

    @Test
    fun `Should throw exception multiple metastatic record`() {
        val followup1 = TestNcrRecordFactory.minimalFollowupRecord()
        val followup2 = TestNcrRecordFactory.minimalFollowupRecord()

        assertThrows<Exception> { NcrFunctions.diagnosisRecord(listOf(followup1, followup2)) }
    }

    @Test
    fun `Should determine min days since diagnosis for minimal multiple records`() {
        val diagnosis = TestNcrRecordFactory.minimalDiagnosisRecord()
        val followup = TestNcrRecordFactory.minimalFollowupRecord()

        assertThat(NcrFunctions.recordsWithMinDaysSinceDiagnosis(listOf(diagnosis, followup))).isEqualTo(
            mapOf(
                diagnosis to 0,
                followup to 0
            )
        )
    }

    @Test
    fun `Should determine min days since diagnosis for proper multiple records`() {
        val diagnosis = TestNcrRecordFactory.properDiagnosisRecord()
        val followup1 = TestNcrRecordFactory.properFollowupRecord1()
        val followup2 = TestNcrRecordFactory.properFollowupRecord2()

        assertThat(NcrFunctions.recordsWithMinDaysSinceDiagnosis(listOf(diagnosis, followup1, followup2))).isEqualTo(
            mapOf(
                diagnosis to 0,
                followup1 to 50,
                followup2 to 100
            )
        )
    }
}