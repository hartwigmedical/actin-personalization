package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ValidTreatmentDataFilterTest {

    private val filter = ValidTreatmentDataFilter(true)
    private val diagnosis = TestNcrRecordFactory.minimalDiagnosisRecord()

    @Test
    fun `Should return true for valid treatment intervals`() {
        val records = listOf(
            diagnosis.copy(
                treatment = diagnosis.treatment.copy(
                    primarySurgery = diagnosis.treatment.primarySurgery.copy(chir = 1, chirInt1 = 10, chirType1 = 1),
                    primaryRadiotherapy = diagnosis.treatment.primaryRadiotherapy.copy(rt = 1, rtStartInt1 = 5, rtType1 = 1),
                    systemicTreatment = diagnosis.treatment.systemicTreatment.copy(
                        chemo = 1,
                        target = 0,
                        systCode1 = "code1",
                        systStartInt1 = 5
                    )
                )
            )
        )

        assertThat(filter.apply(records)).isTrue()
    }

    @Test
    fun `Should return false for invalid treatment intervals`() {
        val records = listOf(
            diagnosis.copy(
                treatment = diagnosis.treatment.copy(
                    primarySurgery = diagnosis.treatment.primarySurgery.copy(chir = 1, chirInt1 = 5, chirType1 = 1),
                    primaryRadiotherapy = diagnosis.treatment.primaryRadiotherapy.copy(rt = 1, rtStartInt1 = 10),
                    systemicTreatment = diagnosis.treatment.systemicTreatment.copy(chemo = 1, systStartInt1 = 10)
                )
            )
        )

        assertThat(filter.apply(records)).isFalse()
    }

    @Test
    fun `Should return true for valid surgery data`() {
        val records = listOf(
            diagnosis.copy(
                treatment = diagnosis.treatment.copy(
                    primarySurgery = diagnosis.treatment.primarySurgery.copy(chir = 1, chirType1 = 1, chirType2 = null)
                )
            )
        )

        assertThat(filter.apply(records)).isTrue()
    }

    @Test
    fun `Should return false for invalid surgery data`() {
        val records = listOf(
            diagnosis.copy(
                treatment = diagnosis.treatment.copy(
                    primarySurgery = diagnosis.treatment.primarySurgery.copy(chir = 1, chirType1 = null, chirType2 = null)
                )
            )
        )

        assertThat(filter.apply(records)).isFalse()
    }

    @Test
    fun `Should return true for valid radiotherapy data`() {
        val records = listOf(
            diagnosis.copy(
                treatment = diagnosis.treatment.copy(
                    primaryRadiotherapy = diagnosis.treatment.primaryRadiotherapy.copy(
                        rt = 1,
                        chemort = 0,
                        rtType1 = 1,
                        rtType2 = null
                    )
                )
            )
        )

        assertThat(filter.apply(records)).isTrue()
    }

    @Test
    fun `Should return false for invalid radiotherapy data`() {
        val records = listOf(
            diagnosis.copy(
                treatment = diagnosis.treatment.copy(
                    primaryRadiotherapy = diagnosis.treatment.primaryRadiotherapy.copy(
                        rt = 1,
                        chemort = 0,
                        rtType1 = null,
                        rtType2 = null
                    )
                )
            )
        )

        assertThat(filter.apply(records)).isFalse()
    }

    @Test
    fun `Should return true for valid gastro resection data`() {
        val records = listOf(
            diagnosis.copy(
                treatment = diagnosis.treatment.copy(
                    gastroenterologyResection = diagnosis.treatment.gastroenterologyResection.copy(
                        mdlRes = 1,
                        mdlResType1 = 1,
                        mdlResType2 = null
                    )
                )
            )
        )

        assertThat(filter.apply(records)).isTrue()
    }

    @Test
    fun `Should return false for invalid gastro resection data`() {
        val records = listOf(
            diagnosis.copy(
                treatment = diagnosis.treatment.copy(
                    gastroenterologyResection = diagnosis.treatment.gastroenterologyResection.copy(
                        mdlRes = 1,
                        mdlResType1 = null,
                        mdlResType2 = null
                    )
                )
            )
        )

        assertThat(filter.apply(records)).isFalse()
    }

    @Test
    fun `Should return true for valid systemic treatment data`() {
        val records = listOf(
            diagnosis.copy(
                treatment = diagnosis.treatment.copy(
                    systemicTreatment = diagnosis.treatment.systemicTreatment.copy(
                        chemo = 1,
                        target = 1,
                        systCode1 = "code1",
                        systSchemanum1 = null
                    )
                )
            )
        )

        assertThat(filter.apply(records)).isTrue()
    }

    @Test
    fun `Should return false for invalid systemic treatment data`() {
        val records = listOf(
            diagnosis.copy(
                treatment = diagnosis.treatment.copy(
                    systemicTreatment = diagnosis.treatment.systemicTreatment.copy(chemo = 1, target = 0, systCode1 = null)
                )
            )
        )

        assertThat(filter.apply(records)).isFalse()
    }

    @Test
    fun `Should return true for valid system codes`() {
        val records = listOf(
            diagnosis.copy(
                treatment = diagnosis.treatment.copy(
                    systemicTreatment = diagnosis.treatment.systemicTreatment.copy(
                        chemo = 1,
                        target = 0,
                        systCode1 = "code1",
                        systSchemanum1 = null
                    )
                )
            )
        )

        assertThat(filter.apply(records)).isTrue()
    }

    @Test
    fun `Should return false for invalid system codes`() {
        val records = listOf(
            diagnosis.copy(
                treatment = diagnosis.treatment.copy(
                    systemicTreatment = diagnosis.treatment.systemicTreatment.copy(systCode1 = null, systSchemanum1 = 1)
                )
            )
        )

        assertThat(filter.apply(records)).isFalse()
    }
}
