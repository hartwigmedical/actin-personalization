package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TreatmentRecordFilterTest {
    private val filter = TreatmentRecordFilter(true)
    private val minimalDiagnosisRecord = TestNcrRecordFactory.minimalDiagnosisRecord()
    
    @Test
    fun `Should return true for valid intervals`() {
        val records = listOf(minimalDiagnosisRecord.copy(
            treatment = minimalDiagnosisRecord.treatment.copy(
                primarySurgery = minimalDiagnosisRecord.treatment.primarySurgery.copy(chir = 1, chirInt1 = 10),
                primaryRadiotherapy = minimalDiagnosisRecord.treatment.primaryRadiotherapy.copy(rt = 1, rtStartInt1 = 5),
                systemicTreatment = minimalDiagnosisRecord.treatment.systemicTreatment.copy(chemo = 1, systStartInt1 = 5)
            )
        ))
        assertThat(filter.hasConsistentPreAndPostSurgicalIntervalsForChemoAndRadiotherapy(records)).isTrue()
    }

    @Test
    fun `Should return false for invalid intervals`() {
        val records = listOf(minimalDiagnosisRecord.copy(
            treatment = minimalDiagnosisRecord.treatment.copy(
                primarySurgery = minimalDiagnosisRecord.treatment.primarySurgery.copy(chir = 1, chirInt1 = 5),
                primaryRadiotherapy = minimalDiagnosisRecord.treatment.primaryRadiotherapy.copy(rt = 1, rtStartInt1 = 10),
                systemicTreatment = minimalDiagnosisRecord.treatment.systemicTreatment.copy(chemo = 1, systStartInt1 = 10)
            )
        ))
        assertThat(filter.hasConsistentPreAndPostSurgicalIntervalsForChemoAndRadiotherapy(records)).isFalse()
    }

    @Test
    fun `Should return true for valid surgery data`() {
        val records = listOf(minimalDiagnosisRecord.copy(
            treatment = minimalDiagnosisRecord.treatment.copy(
                primarySurgery = minimalDiagnosisRecord.treatment.primarySurgery.copy(chir = 1, chirType1 = 1, chirType2 = null)
            )
        ))
        assertThat(filter.hasValidPrimarySurgery(records)).isTrue()
    }

    @Test
    fun `Should return false for invalid surgery data`() {
        val records = listOf(minimalDiagnosisRecord.copy(
            treatment = minimalDiagnosisRecord.treatment.copy(
                primarySurgery = minimalDiagnosisRecord.treatment.primarySurgery.copy(chir = 1, chirType1 = null, chirType2 = null)
            )
        ))
        assertThat(filter.hasValidPrimarySurgery(records)).isFalse()
    }

    @Test
    fun `Should return true for valid radiotherapy data`() {
        val records = listOf(minimalDiagnosisRecord.copy(
            treatment = minimalDiagnosisRecord.treatment.copy(
                primaryRadiotherapy = minimalDiagnosisRecord.treatment.primaryRadiotherapy.copy(rt = 1, chemort = 0, rtType1 = 1, rtType2 = null)
            )
        ))
        assertThat(filter.hasValidPrimaryRadiotherapy(records)).isTrue()
    }

    @Test
    fun `Should return false for invalid radiotherapy data`() {
        val records = listOf(minimalDiagnosisRecord.copy(
            treatment = minimalDiagnosisRecord.treatment.copy(
                primaryRadiotherapy = minimalDiagnosisRecord.treatment.primaryRadiotherapy.copy(rt = 1, chemort = 0, rtType1 = null, rtType2 = null)
            )
        ))
        assertThat(filter.hasValidPrimaryRadiotherapy(records)).isFalse()
    }

    @Test
    fun `Should return true for valid gastro resection data`() {
        val records = listOf(minimalDiagnosisRecord.copy(
            treatment = minimalDiagnosisRecord.treatment.copy(
                gastroenterologyResection = minimalDiagnosisRecord.treatment.gastroenterologyResection.copy(mdlRes = 1, mdlResType1 = 1, mdlResType2 = null)
            )
        ))
        assertThat(filter.hasValidGastroResection(records)).isTrue()
    }

    @Test
    fun `Should return false for invalid gastro resection data`() {
        val records = listOf(minimalDiagnosisRecord.copy(
            treatment = minimalDiagnosisRecord.treatment.copy(
                gastroenterologyResection = minimalDiagnosisRecord.treatment.gastroenterologyResection.copy(mdlRes = 1, mdlResType1 = null, mdlResType2 = null)
            )
        ))
        assertThat(filter.hasValidGastroResection(records)).isFalse()
    }

    @Test
    fun `Should return true for valid systemic treatment data`() {
        val records = listOf(minimalDiagnosisRecord.copy(
            treatment = minimalDiagnosisRecord.treatment.copy(
                systemicTreatment = minimalDiagnosisRecord.treatment.systemicTreatment.copy(chemo = 1, target = 1, systCode1 = "code1", systSchemanum1 = null)
            )
        ))
        assertThat(filter.hasValidSystemicTreatment(records)).isTrue()
    }

    @Test
    fun `Should return false for invalid systemic treatment data`() {
        val records = listOf(minimalDiagnosisRecord.copy(
            treatment = minimalDiagnosisRecord.treatment.copy(
                systemicTreatment = minimalDiagnosisRecord.treatment.systemicTreatment.copy(chemo = 1, target = 0, systCode1 = null)
            )
        ))
        assertThat(filter.hasValidSystemicTreatment(records)).isFalse()
    }

    @Test
    fun `Should return true for valid system codes`() {
        val records = listOf(minimalDiagnosisRecord.copy(
            treatment = minimalDiagnosisRecord.treatment.copy(
                systemicTreatment = minimalDiagnosisRecord.treatment.systemicTreatment.copy(systCode1 = "code1", systSchemanum1 = null)
            )
        ))
        assertThat(filter.hasValidSystemCodes(records)).isTrue()
    }

    @Test
    fun `Should return false for invalid system codes`() {
        val records = listOf(minimalDiagnosisRecord.copy(
            treatment = minimalDiagnosisRecord.treatment.copy(
                systemicTreatment = minimalDiagnosisRecord.treatment.systemicTreatment.copy(systCode1 = null, systSchemanum1 = 1)
            )
        ))
        assertThat(filter.hasValidSystemCodes(records)).isFalse()
    }
}
