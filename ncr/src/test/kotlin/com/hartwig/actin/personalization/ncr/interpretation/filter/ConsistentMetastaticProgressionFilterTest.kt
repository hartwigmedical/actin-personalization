package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ConsistentMetastaticProgressionFilterTest {

    private val filter = ConsistentMetastaticProgressionFilter(true)
    private val minimal = TestNcrRecordFactory.minimalDiagnosisRecord()
    
    @Test
    fun `Should return true for at least one metastases without progression in case of detection at start`() {
        val records = listOf(
            minimal.copy(
                identification = minimal.identification.copy(metaEpis = 1),
                metastaticDiagnosis = minimal.metastaticDiagnosis.copy(
                    metaProg1 = 0,
                    metaProg2 = 1
                )
            )
        )
        assertThat(filter.apply(records)).isTrue()
    }

    @Test
    fun `Should return false for having no progression metastases in an episode with metastases during progression`() {
        val records = listOf(
            minimal.copy(
                identification = minimal.identification.copy(metaEpis = 2),
                metastaticDiagnosis = minimal.metastaticDiagnosis.copy(
                    metaProg1 = 0,
                    metaProg2 = 0,
                    metaProg3 = 0,
                    metaProg4 = 0,
                    metaProg5 = 0,
                    metaProg6 = 0,
                    metaProg7 = 0,
                    metaProg8 = 0,
                    metaProg9 = 0,
                    metaProg10 = 0
                )
            )
        )
        assertThat(filter.apply(records)).isFalse()
    }

    @Test
    fun `Should return true for consistent progression in an episode with metastases at progression`() {
        val records = listOf(
            minimal.copy(
                identification = minimal.identification.copy(metaEpis = 2),
                metastaticDiagnosis = minimal.metastaticDiagnosis.copy(
                    metaProg1 = 1,
                    metaProg2 = 0,
                    metaProg3 = 0,
                    metaProg4 = 0,
                    metaProg5 = 0,
                    metaProg6 = 0,
                    metaProg7 = 0,
                    metaProg8 = 0,
                    metaProg9 = 0,
                    metaProg10 = 0
                )
            )
        )
        assertThat(filter.apply(records)).isTrue()
    }
}