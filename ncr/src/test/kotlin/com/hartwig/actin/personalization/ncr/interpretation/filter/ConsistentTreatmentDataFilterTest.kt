package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ConsistentTreatmentDataFilterTest {

    private val filter = ConsistentTreatmentDataFilter(true)
    private val diagnosis = TestNcrRecordFactory.minimalDiagnosisRecord()

    @Test
    fun `Should return true when there is no treatment data at all`() {
        val records = listOf(diagnosis)
        assertThat(filter.apply(records)).isTrue()
    }

    @Test
    fun `Should return false when treatment is supposed to be there yet no interventions found`() {
        val records = listOf(
            diagnosis.copy(
                treatment = diagnosis.treatment.copy(
                    tumgerichtTher = 1,
                    primarySurgery = diagnosis.treatment.primarySurgery.copy(chir = null),
                    primaryRadiotherapy = diagnosis.treatment.primaryRadiotherapy.copy(rt = 0, chemort = 0),
                    gastroenterologyResection = diagnosis.treatment.gastroenterologyResection.copy(mdlRes = null),
                    hipec = diagnosis.treatment.hipec.copy(hipec = null),
                    systemicTreatment = diagnosis.treatment.systemicTreatment.copy(chemo = 0, target = 0),
                    metastaticSurgery = diagnosis.treatment.metastaticSurgery.copy(metaChirInt1 = null),
                    metastaticRadiotherapy = diagnosis.treatment.metastaticRadiotherapy.copy(metaRtCode1 = null)
                )
            )
        )
        assertThat(filter.apply(records)).isFalse()
    }
}