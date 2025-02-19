package com.hartwig.actin.personalization.ncr.interpretation

import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ReferencePatientFactoryTest {

    @Test
    fun `Should create record for minimal NCR input`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord())
        val patients = ReferencePatientFactory.create(records)

        assertThat(patients).hasSize(1)
        assertThat(patients[0].sex).isEqualTo(Sex.FEMALE)
        assertThat(patients[0].tumors).hasSize(1)
    }

    @Test
    fun `Should create record for minimal NCR diagnosis and followup record`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord(), TestNcrRecordFactory.minimalFollowupRecord())
        val patients = ReferencePatientFactory.create(records)

        assertThat(patients).hasSize(1)
        assertThat(patients[0].sex).isEqualTo(Sex.FEMALE)
        assertThat(patients[0].tumors).hasSize(1)
    }

    @Test
    fun `Should create record for proper NCR diagnostic and followup input`() {
        val records = listOf(
            TestNcrRecordFactory.properDiagnosisRecord(),
            TestNcrRecordFactory.properFollowupRecord1(),
            TestNcrRecordFactory.properFollowupRecord2()
        )
        val patients = ReferencePatientFactory.create(records)

        assertThat(patients).hasSize(1)
        assertThat(patients[0].sex).isEqualTo(Sex.FEMALE)
        assertThat(patients[0].tumors).hasSize(1)
    }

    @Test
    fun `Should create multiple patients for multiple minimal diagnostic records`() {
        val baseRecord = TestNcrRecordFactory.minimalDiagnosisRecord()
        val records = listOf(
            baseRecord.copy(identification = baseRecord.identification.copy(keyNkr = 1)),
            baseRecord.copy(identification = baseRecord.identification.copy(keyNkr = 2))
        )
        val patients = ReferencePatientFactory.create(records)
        assertThat(patients).hasSize(2)
    }
}