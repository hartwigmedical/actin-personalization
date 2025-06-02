package com.hartwig.actin.personalization.ncr.interpretation

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NcrRecordReliabilityFilterTest {

    @Test
    fun `Should select entries based on tumgerichtTher and actual treatments`() {
        val baseRecord = TestNcrRecordFactory.minimalDiagnosisRecord()

        val onlyFlagged = baseRecord.copy(
            treatment = baseRecord.treatment.copy(tumgerichtTher = 1)
        )
        assertThat(NcrRecordReliabilityFilter.isReliableTumorRecord(listOf(onlyFlagged))).isFalse()

        val noFlag = baseRecord.copy(
            treatment = baseRecord.treatment.copy(tumgerichtTher = null)
        )
        assertThat(NcrRecordReliabilityFilter.isReliableTumorRecord(listOf(noFlag))).isTrue()

        val validTreatment = baseRecord.copy(
            treatment = baseRecord.treatment.copy(
                tumgerichtTher = 1,
                systemicTreatment = baseRecord.treatment.systemicTreatment.copy(chemo = 1)
            )
        )
        assertThat(NcrRecordReliabilityFilter.isReliableTumorRecord(listOf(validTreatment))).isTrue()
    }

}