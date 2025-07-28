package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PriorTumorRecordFilterTest {
    private val filter = PriorTumorRecordFilter(true)

    @Test
    fun `Should return true when all VERB records have empty prior tumor data`() {
        val records = listOf(
          TestNcrRecordFactory.minimalFollowupRecord()  
        ) 
        assertThat(filter.hasEmptyPriorTumorInVerbEpisode(records)).isTrue()
    }

    @Test
    fun `Should return false when any VERB record has non-empty prior tumor data`() {
        val records = listOf(
            TestNcrRecordFactory.minimalFollowupRecord().copy(
            priorMalignancies = TestNcrRecordFactory.minimalDiagnosisRecord().priorMalignancies.copy(mal1Int = 1)
        ))
        assertThat(filter.hasEmptyPriorTumorInVerbEpisode(records)).isFalse()
    }

    @Test
    fun `Should return true when all malInt values are non-positive`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            priorMalignancies = TestNcrRecordFactory.minimalDiagnosisRecord().priorMalignancies.copy(
                mal1Int = 0, mal2Int = -1, mal3Int = 0, mal4Int = null
            )
        ))
        assertThat(filter.hasNoPositiveValueInMalInt(records)).isTrue()
    }

    @Test
    fun `Should return false when any malInt value is positive`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            priorMalignancies = TestNcrRecordFactory.minimalDiagnosisRecord().priorMalignancies.copy(
                mal1Int = 1, mal2Int = 0, mal3Int = null, mal4Int = null
            )
        ))
        assertThat(filter.hasNoPositiveValueInMalInt(records)).isFalse()
    }

    @Test
    fun `Should return true when all prior tumor data is complete or empty`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            priorMalignancies = TestNcrRecordFactory.minimalDiagnosisRecord().priorMalignancies.copy(
                mal1Int = null, mal1Morf = null, mal1TopoSublok = null, mal1Tumsoort = null, mal1Syst = null,
                mal2Int = 0, mal2Morf = 0, mal2TopoSublok = "A", mal2Tumsoort = 0, mal2Syst = 0,
                mal3Int = null, mal3Morf = null, mal3TopoSublok = null, mal3Tumsoort = null, mal3Syst = null,
                mal4Int = 0, mal4Morf = 0, mal4TopoSublok = "B", mal4Tumsoort = 0, mal4Syst = 0
            )
        ))
        assertThat(filter.hasCompletePriorTumorData(records)).isTrue()
    }

    @Test
    fun `Should return false when any prior tumor data is partially filled`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            priorMalignancies = TestNcrRecordFactory.minimalDiagnosisRecord().priorMalignancies.copy(
                mal1Int = 1, mal1Morf = null, mal1TopoSublok = null, mal1Tumsoort = null, mal1Syst = null
            )
        ))
        assertThat(filter.hasCompletePriorTumorData(records)).isFalse()
    }
}

