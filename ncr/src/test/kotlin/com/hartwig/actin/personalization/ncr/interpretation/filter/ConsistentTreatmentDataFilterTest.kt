package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ConsistentTreatmentDataFilterTest {

    private val filter = ConsistentTreatmentDataFilter(true)
    private val minimal = TestNcrRecordFactory.minimalDiagnosisRecord()

    @Test
    fun `Should return true when there is no treatment data at all`() {
        val records = listOf(minimal)
        assertThat(filter.apply(records)).isTrue()
    }

    @Test
    fun `Should return false when treatment is supposed to be there yet no interventions found`() {
        val records = listOf(
            minimal.copy(
                treatment = minimal.treatment.copy(
                    tumgerichtTher = 1,
                    primarySurgery = minimal.treatment.primarySurgery.copy(chir = null),
                    primaryRadiotherapy = minimal.treatment.primaryRadiotherapy.copy(rt = 0, chemort = 0),
                    gastroenterologyResection = minimal.treatment.gastroenterologyResection.copy(mdlRes = null),
                    hipec = minimal.treatment.hipec.copy(hipec = null),
                    systemicTreatment = minimal.treatment.systemicTreatment.copy(chemo = 0, target = 0),
                    metastaticSurgery = minimal.treatment.metastaticSurgery.copy(metaChirInt1 = null),
                    metastaticRadiotherapy = minimal.treatment.metastaticRadiotherapy.copy(metaRtCode1 = null)
                )
            )
        )
        assertThat(filter.apply(records)).isFalse()
    }
}