package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PriorTumorRecordFilterTest {
    private val logger = mutableListOf<String>()
    private val filter = PriorTumorRecordFilter { logger.add(it) }

    @Test
    fun `Should return true when all VERB records have empty prior tumor data`() {
        val record = TestNcrRecordFactory.minimalFollowupRecord()
        val entry = mapOf(1 to listOf(record)).entries.first()
        assertThat(filter.hasEmptyPriorTumorInVerbEpisode(entry)).isTrue()
    }

    @Test
    fun `Should return false when any VERB record has non-empty prior tumor data`() {
        val record = TestNcrRecordFactory.minimalFollowupRecord().copy(
            priorMalignancies = TestNcrRecordFactory.minimalDiagnosisRecord().priorMalignancies.copy(mal1Int = 1)
        )
        val entry = mapOf(1 to listOf(record)).entries.first()
        assertThat(filter.hasEmptyPriorTumorInVerbEpisode(entry)).isFalse()
    }

    @Test
    fun `Should return true when all malInt values are non-positive`() {
        val record = TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            priorMalignancies = TestNcrRecordFactory.minimalDiagnosisRecord().priorMalignancies.copy(
                mal1Int = 0, mal2Int = -1, mal3Int = 0, mal4Int = null
            )
        )
        val entry = mapOf(1 to listOf(record)).entries.first()
        assertThat(filter.hasNoPositiveValueInMalInt(entry)).isTrue()
    }

    @Test
    fun `Should return false when any malInt value is positive`() {
        val record = TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            priorMalignancies = TestNcrRecordFactory.minimalDiagnosisRecord().priorMalignancies.copy(
                mal1Int = 1, mal2Int = 0, mal3Int = null, mal4Int = null
            )
        )
        val entry = mapOf(1 to listOf(record)).entries.first()
        assertThat(filter.hasNoPositiveValueInMalInt(entry)).isFalse()
    }

    @Test
    fun `Should return true when all prior tumor data is complete or empty`() {
        val record = TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            priorMalignancies = TestNcrRecordFactory.minimalDiagnosisRecord().priorMalignancies.copy(
                mal1Int = null, mal1Morf = null, mal1TopoSublok = null, mal1Tumsoort = null, mal1Syst = null,
                mal2Int = 0, mal2Morf = 0, mal2TopoSublok = "A", mal2Tumsoort = 0, mal2Syst = 0,
                mal3Int = null, mal3Morf = null, mal3TopoSublok = null, mal3Tumsoort = null, mal3Syst = null,
                mal4Int = 0, mal4Morf = 0, mal4TopoSublok = "B", mal4Tumsoort = 0, mal4Syst = 0
            )
        )
        val entry = mapOf(1 to listOf(record)).entries.first()
        assertThat(filter.hasCompletePriorTumorData(entry)).isTrue()
    }

    @Test
    fun `Should return false when any prior tumor data is partially filled`() {
        val record = TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            priorMalignancies = TestNcrRecordFactory.minimalDiagnosisRecord().priorMalignancies.copy(
                mal1Int = 1, mal1Morf = null, mal1TopoSublok = null, mal1Tumsoort = null, mal1Syst = null
            )
        )
        val entry = mapOf(1 to listOf(record)).entries.first()
        assertThat(filter.hasCompletePriorTumorData(entry)).isFalse()
    }
}

