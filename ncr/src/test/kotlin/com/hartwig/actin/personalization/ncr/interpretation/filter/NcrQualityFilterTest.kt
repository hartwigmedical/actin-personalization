package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NcrQualityFilterTest {

    @Test
    fun `Should select entries with valid treatment data`() {
        val baseRecord = TestNcrRecordFactory.minimalDiagnosisRecord()

        val onlyFlagged = baseRecord.copy(
            treatment = baseRecord.treatment.copy(
                tumgerichtTher = 1,
                systemicTreatment = baseRecord.treatment.systemicTreatment.copy(chemo = 0, target = 0),
                primarySurgery = baseRecord.treatment.primarySurgery.copy(chir = null),
                metastaticSurgery = baseRecord.treatment.metastaticSurgery.copy(metaChirInt1 = null),
                primaryRadiotherapy = baseRecord.treatment.primaryRadiotherapy.copy(rt = 0, chemort = 0),
                metastaticRadiotherapy = baseRecord.treatment.metastaticRadiotherapy.copy(metaRtCode1 = null),
                gastroenterologyResection = baseRecord.treatment.gastroenterologyResection.copy(mdlRes = null),
                hipec = baseRecord.treatment.hipec.copy(hipec = null)
            )
        )

        assertThat(NcrQualityFilter.isReliableTumorRecordSet(listOf(onlyFlagged))).isFalse()

        val noFlag = onlyFlagged.copy(
            treatment = onlyFlagged.treatment.copy(tumgerichtTher = null)
        )
        assertThat(NcrQualityFilter.isReliableTumorRecordSet(listOf(noFlag))).isTrue()

        val validTreatment = onlyFlagged.copy(
            treatment = onlyFlagged.treatment.copy(
                tumgerichtTher = 1,
                systemicTreatment = onlyFlagged.treatment.systemicTreatment.copy(chemo = 1)
            )
        )
        assertThat(NcrQualityFilter.isReliableTumorRecordSet(listOf(validTreatment))).isTrue()
    }
}