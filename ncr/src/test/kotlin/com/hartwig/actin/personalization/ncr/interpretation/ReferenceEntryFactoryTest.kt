package com.hartwig.actin.personalization.ncr.interpretation

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ReferenceEntryFactoryTest {

    @Test
    fun `Should create record for minimal NCR input`() {
        val records = TestNcrRecordFactory.minimalEntryRecords()

        assertThat(ReferenceEntryFactory.create(records)).hasSize(1)
    }

    @Test
    fun `Should create record for minimal NCR diagnosis and followup record`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord(), TestNcrRecordFactory.minimalFollowupRecord())

        assertThat(ReferenceEntryFactory.create(records)).hasSize(1)
    }

    @Test
    fun `Should create record for proper NCR diagnostic and followup input`() {
        val records = listOf(
            TestNcrRecordFactory.properDiagnosisRecord(),
            TestNcrRecordFactory.properFollowupRecord1(),
            TestNcrRecordFactory.properFollowupRecord2()
        )

        assertThat(ReferenceEntryFactory.create(records)).hasSize(1)
    }

    @Test
    fun `Should create multiple entries for multiple patient records`() {
        val baseRecord1 = TestNcrRecordFactory.minimalDiagnosisRecord()
        val baseRecord2 = TestNcrRecordFactory.minimalFollowupRecord()
        val baseRecord3 = TestNcrRecordFactory.properDiagnosisRecord()
        val baseRecord4 = TestNcrRecordFactory.properFollowupRecord2()
        val baseRecord5 = TestNcrRecordFactory.minimalDiagnosisRecord()
        val baseRecord6 = TestNcrRecordFactory.minimalFollowupRecord()

        val records = listOf(
            baseRecord1.copy(identification = baseRecord1.identification.copy(keyNkr = 1, keyZid = 1)),
            baseRecord2.copy(identification = baseRecord2.identification.copy(keyNkr = 1, keyZid = 1)),
            baseRecord3.copy(identification = baseRecord3.identification.copy(keyNkr = 2, keyZid = 2)),
            baseRecord4.copy(identification = baseRecord4.identification.copy(keyNkr = 2, keyZid = 2)),
            baseRecord5.copy(identification = baseRecord5.identification.copy(keyNkr = 2, keyZid = 3)),
            baseRecord6.copy(identification = baseRecord6.identification.copy(keyNkr = 2, keyZid = 3))
        )

        assertThat(ReferenceEntryFactory.create(records)).hasSize(3)
    }
}