package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NcrQualityFilterTest {

    @Test
    fun `Should select entries with valid treatment data`() {
        val baseRecord = TestNcrRecordFactory.minimalDiagnosisRecord()

        val onlyFlagged = baseRecord.copy(treatment = baseRecord.treatment.copy(tumgerichtTher = 1))
        assertThat(NcrQualityFilter.isReliableTumorRecordSet(listOf(onlyFlagged))).isFalse()

        val noFlag = baseRecord.copy(treatment = baseRecord.treatment.copy(tumgerichtTher = null))
        assertThat(NcrQualityFilter.isReliableTumorRecordSet(listOf(noFlag))).isTrue()

        val validTreatment = baseRecord.copy(
            treatment = baseRecord.treatment.copy(
                tumgerichtTher = 1,
                systemicTreatment = baseRecord.treatment.systemicTreatment.copy(chemo = 1)
            )
        )
        assertThat(NcrQualityFilter.isReliableTumorRecordSet(listOf(validTreatment))).isTrue()
    }
}