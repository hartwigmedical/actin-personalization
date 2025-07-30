package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TreatmentRecordFilterTest {
    private val filter = TreatmentRecordFilter(true)

    @Test
    fun `Should returns true for record with treatment`() {
        val treatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.copy(
            primarySurgery = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.primarySurgery.copy(chir = 1)
        )
        assertThat(filter.hasAtLeastOneTreatment(treatment)).isTrue()
    }

    @Test
    fun `Should returns false for record with no treatment`() {
        val treatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.copy(
            primarySurgery = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.primarySurgery.copy(chir = 0),
            primaryRadiotherapy = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.primaryRadiotherapy.copy(rt = 0),
            systemicTreatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.systemicTreatment.copy(chemo = 0),
            hipec = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.hipec.copy(hipec = 0)
        )
        assertThat(filter.hasAtLeastOneTreatment(treatment)).isFalse()
    }

    @Test
    fun `Should returns true for record with treatment and no reason`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            treatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.copy(
                primarySurgery = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.primarySurgery.copy(chir = 1),
                geenTherReden = null
            )
        ))
        assertThat(filter.hasConsistentReasonRefrainingTreatment(records)).isTrue()
    }

    @Test
    fun `Should returns false for record with treatment and reason`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            treatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.copy(
                primarySurgery = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.primarySurgery.copy(chir = 1),
                geenTherReden = 1
            )
        ))
        assertThat(filter.hasConsistentReasonRefrainingTreatment(records)).isFalse()
    }

    @Test
    fun `Should returns true for valid intervals`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            treatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.copy(
                primarySurgery = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.primarySurgery.copy(chir = 1, chirInt1 = 10),
                primaryRadiotherapy = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.primaryRadiotherapy.copy(rt = 1, rtStartInt1 = 5),
                systemicTreatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.systemicTreatment.copy(chemo = 1, systStartInt1 = 5)
            )
        ))
        assertThat(filter.hasValidTherprepostCode(records)).isTrue()
    }

    @Test
    fun `Should returns false for invalid intervals`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            treatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.copy(
                primarySurgery = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.primarySurgery.copy(chir = 1, chirInt1 = 5),
                primaryRadiotherapy = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.primaryRadiotherapy.copy(rt = 1, rtStartInt1 = 10),
                systemicTreatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.systemicTreatment.copy(chemo = 1, systStartInt1 = 10)
            )
        ))
        assertThat(filter.hasValidTherprepostCode(records)).isFalse()
    }

    @Test
    fun `Should returns true for valid surgery data`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            treatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.copy(
                primarySurgery = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.primarySurgery.copy(chir = 1, chirType1 = 1, chirType2 = null)
            )
        ))
        assertThat(TreatmentRecordFilter(true).isPrimarySurgeryValid(records)).isTrue()
    }

    @Test
    fun `Should returns false for invalid surgery data`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            treatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.copy(
                primarySurgery = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.primarySurgery.copy(chir = 1, chirType1 = null, chirType2 = null)
            )
        ))
        assertThat(TreatmentRecordFilter(true).isPrimarySurgeryValid(records)).isFalse()
    }

    @Test
    fun `Should returns true for valid radiotherapy data`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            treatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.copy(
                primaryRadiotherapy = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.primaryRadiotherapy.copy(rt = 1, chemort = 0, rtType1 = 1, rtType2 = null)
            )
        ))
        assertThat(TreatmentRecordFilter(true).isPrimaryRadiotherapyValid(records)).isTrue()
    }

    @Test
    fun `Should returns false for invalid radiotherapy data`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            treatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.copy(
                primaryRadiotherapy = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.primaryRadiotherapy.copy(rt = 1, chemort = 0, rtType1 = null, rtType2 = null)
            )
        ))
        assertThat(TreatmentRecordFilter(true).isPrimaryRadiotherapyValid(records)).isFalse()
    }

    @Test
    fun `Should returns true for valid gastro resection data`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            treatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.copy(
                gastroenterologyResection = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.gastroenterologyResection.copy(mdlRes = 1, mdlResType1 = 1, mdlResType2 = null)
            )
        ))
        assertThat(TreatmentRecordFilter(true).isGastroResectionValid(records)).isTrue()
    }

    @Test
    fun `Should returns false for invalid gastro resection data`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            treatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.copy(
                gastroenterologyResection = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.gastroenterologyResection.copy(mdlRes = 1, mdlResType1 = null, mdlResType2 = null)
            )
        ))
        assertThat(TreatmentRecordFilter(true).isGastroResectionValid(records)).isFalse()
    }

    @Test
    fun `Should returns true for valid systemic treatment data`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            treatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.copy(
                systemicTreatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.systemicTreatment.copy(chemo = 1, target = 1, systCode1 = "code1", systSchemanum1 = null)
            )
        ))
        assertThat(TreatmentRecordFilter(true).isSystemicTreatmentValid(records)).isTrue()
    }

    @Test
    fun `Should returns false for invalid systemic treatment data`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            treatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.copy(
                systemicTreatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.systemicTreatment.copy(chemo = 1, target = 0, systCode1 = null)
            )
        ))
        assertThat(TreatmentRecordFilter(true).isSystemicTreatmentValid(records)).isFalse()
    }

    @Test
    fun `Should returns true for valid system codes`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            treatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.copy(
                systemicTreatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.systemicTreatment.copy(systCode1 = "code1", systSchemanum1 = null)
            )
        ))
        assertThat(filter.hasValidSystemCodes(records)).isTrue()
    }

    @Test
    fun `Should returns false for invalid system codes`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            treatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.copy(
                systemicTreatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.systemicTreatment.copy(systCode1 = null, systSchemanum1 = 1)
            )
        ))
        assertThat(filter.hasValidSystemCodes(records)).isFalse()
    }
}
