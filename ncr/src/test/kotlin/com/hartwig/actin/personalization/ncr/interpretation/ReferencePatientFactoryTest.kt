package com.hartwig.actin.personalization.ncr.interpretation

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ReferencePatientFactoryTest {

    @Test
    fun `Should create record for minimal NCR input`() {
        val patients = ReferencePatientFactory.default().create(listOf(TestNcrRecordFactory.minimalDiagnosisRecord()))

        assertThat(patients).hasSize(1)
    }
}