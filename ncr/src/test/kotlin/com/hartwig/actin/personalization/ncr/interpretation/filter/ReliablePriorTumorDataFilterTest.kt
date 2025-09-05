package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ReliablePriorTumorDataFilterTest {

    private val filter = ReliablePriorTumorDataFilter(true)
    private val diagnosis = TestNcrRecordFactory.minimalDiagnosisRecord()
    private val followup = TestNcrRecordFactory.minimalFollowupRecord()

    @Test
    fun `Should return true when all followup records have empty prior tumor data`() {
        val records = listOf(followup)
        
        assertThat(filter.apply(records)).isTrue()
    }

    @Test
    fun `Should return false when any followup record has non-empty prior tumor data`() {
        val records = listOf(followup.copy(priorMalignancies = diagnosis.priorMalignancies.copy(mal1Int = 1)))
        
        assertThat(filter.apply(records)).isFalse()
    }
    
    @Test
    fun `Should return false when any malInt value is positive`() {
        val records = listOf(
            diagnosis.copy(
                priorMalignancies = diagnosis.priorMalignancies.copy(
                    mal1Int = 2, mal1Morf = null, mal1TopoSublok = null, mal1Tumsoort = null, mal1Syst = null,
                    mal2Int = -1, mal2Morf = 0, mal2TopoSublok = "A", mal2Tumsoort = 0, mal2Syst = 0,
                    mal3Int = null, mal3Morf = null, mal3TopoSublok = null, mal3Tumsoort = null, mal3Syst = null,
                    mal4Int = 0, mal4Morf = 0, mal4TopoSublok = "B", mal4Tumsoort = 0, mal4Syst = 0
                )
            )
        )
        
        assertThat(filter.apply(records)).isFalse()
    }

    @Test
    fun `Should return true when all prior tumor data is complete or empty`() {
        val records = listOf(
            diagnosis.copy(
                priorMalignancies = diagnosis.priorMalignancies.copy(
                    mal1Int = null, mal1Morf = null, mal1TopoSublok = null, mal1Tumsoort = null, mal1Syst = null,
                    mal2Int = 0, mal2Morf = 0, mal2TopoSublok = "A", mal2Tumsoort = 0, mal2Syst = 0,
                    mal3Int = null, mal3Morf = null, mal3TopoSublok = null, mal3Tumsoort = null, mal3Syst = null,
                    mal4Int = 0, mal4Morf = 0, mal4TopoSublok = "B", mal4Tumsoort = 0, mal4Syst = 0
                )
            )
        )
        
        assertThat(filter.apply(records)).isTrue()
    }

    @Test
    fun `Should return false when any prior tumor data is partially filled`() {
        val records = listOf(
            diagnosis.copy(
                priorMalignancies = diagnosis.priorMalignancies.copy(
                    mal1Int = 1, mal1Morf = null, mal1TopoSublok = null, mal1Tumsoort = null, mal1Syst = null
                )
            )
        )
        
        assertThat(filter.apply(records)).isFalse()
    }
}

