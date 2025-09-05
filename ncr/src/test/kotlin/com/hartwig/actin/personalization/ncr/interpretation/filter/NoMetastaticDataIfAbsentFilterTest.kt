package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NoMetastaticDataIfAbsentFilterTest {

    private val filter = NoMetastaticDataIfAbsentFilter(true)
    private val diagnosis = TestNcrRecordFactory.minimalDiagnosisRecord()
    
    @Test
    fun `Should return true when metastatic fields are empty if detection not present`() {
        val records = listOf(diagnosis.copy(identification = diagnosis.identification.copy(metaEpis = 0)))
        assertThat(filter.apply(records)).isTrue()
    }

    @Test
    fun `Should return false when metastatic fields are not empty if detection not present`() {
        val records = listOf(
            diagnosis.copy(
                identification = diagnosis.identification.copy(metaEpis = 0),
                metastaticDiagnosis = diagnosis.metastaticDiagnosis.copy(metaProg1 = 1),
                treatment = diagnosis.treatment.copy(
                    metastaticSurgery = diagnosis.treatment.metastaticSurgery.copy(metaChirCode1 = "surgery"),
                    metastaticRadiotherapy = diagnosis.treatment.metastaticRadiotherapy.copy(metaRtCode1 = "radiotherapy")
                )
            )
        )
        assertThat(filter.apply(records)).isFalse()
    }
}