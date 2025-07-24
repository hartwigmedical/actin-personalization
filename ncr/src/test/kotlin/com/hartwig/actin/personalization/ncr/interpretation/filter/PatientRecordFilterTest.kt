package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PatientRecordFilterTest {
    private val logger = mutableListOf<String>()
    private val filter = PatientRecordFilter { logger.add(it) }

    @Test
    fun `Should return true when treatment is valid`() {
        val record = TestNcrRecordFactory.minimalDiagnosisRecord()
        val entry = mapOf(1 to listOf(record)).entries.first()
        assertThat(filter.hasValidTreatmentData(entry)).isTrue()
    }

    @Test
    fun `Should return false when treatment is invalid`() {
        val record = TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            treatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.copy(
                tumgerichtTher = 1,
                primarySurgery = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.primarySurgery.copy(chir = null),
                primaryRadiotherapy = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.primaryRadiotherapy.copy(
                    rt = 0,
                    chemort = 0
                ),
                gastroenterologyResection = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.gastroenterologyResection.copy(
                    mdlRes = null
                ),
                hipec = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.hipec.copy(hipec = null),
                systemicTreatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.systemicTreatment.copy(
                    chemo = 0,
                    target = 0
                ),
                metastaticSurgery = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.metastaticSurgery.copy(
                    metaChirInt1 = null
                ),
                metastaticRadiotherapy = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.metastaticRadiotherapy.copy(
                    metaRtCode1 = null
                )
            )
        )
        val entry = mapOf(1 to listOf(record)).entries.first()
        assertThat(filter.hasValidTreatmentData(entry)).isFalse()
    }

    @Test
    fun `Should return true when all records have identical sex`() {
        val record1 = TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            patientCharacteristics = TestNcrRecordFactory.minimalDiagnosisRecord().patientCharacteristics.copy(gesl = 1)
        )
        val record2 = record1.copy()
        val entry = mapOf(1 to listOf(record1, record2)).entries.first()
        assertThat(filter.hasIdentialSex(entry)).isTrue()
    }

    @Test
    fun `Should return false when records have different sex`() {
        val record1 = TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            patientCharacteristics = TestNcrRecordFactory.minimalDiagnosisRecord().patientCharacteristics.copy(gesl = 1)
        )
        val record2 = record1.copy(patientCharacteristics = record1.patientCharacteristics.copy(gesl = 2))
        val entry = mapOf(1 to listOf(record1, record2)).entries.first()
        assertThat(filter.hasIdentialSex(entry)).isFalse()
    }

    @Test
    fun `Should return true when there is exactly one diagnosis`() {
        val record = TestNcrRecordFactory.minimalDiagnosisRecord()
        val entry = mapOf(1 to listOf(record)).entries.first()
        assertThat(filter.hasExactlyOneDiagnosis(entry)).isTrue()
    }

    @Test
    fun `Should return false when there are zero or multiple diagnoses`() {
        val record = TestNcrRecordFactory.minimalDiagnosisRecord()
        val followUp = record.copy(identification = record.identification.copy(epis = "NOT DIA"))
        val entryNone = mapOf(1 to listOf(followUp)).entries.first()
        val entryMultiple = mapOf(1 to listOf(record, record)).entries.first()
        assertThat(filter.hasExactlyOneDiagnosis(entryNone)).isFalse()
        assertThat(filter.hasExactlyOneDiagnosis(entryMultiple)).isFalse()
    }

    @Test
    fun `Should return true when all DIA records have vital status`() {
        val record = TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            patientCharacteristics = TestNcrRecordFactory.minimalDiagnosisRecord().patientCharacteristics.copy(
                vitStat = 1,
                vitStatInt = 1
            )
        )
        val entry = mapOf(1 to listOf(record)).entries.first()
        assertThat(filter.hasVitalStatusForDIARecords(entry)).isTrue()
    }

    @Test
    fun `Should return false when any DIA record is missing vital status`() {
        val record = TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            patientCharacteristics = TestNcrRecordFactory.minimalDiagnosisRecord().patientCharacteristics.copy(
                vitStat = null,
                vitStatInt = null
            )
        )
        val entry = mapOf(1 to listOf(record)).entries.first()
        assertThat(filter.hasVitalStatusForDIARecords(entry)).isFalse()
    }

    @Test
    fun `Should return true when all VERB records have empty vital status`() {
        val record = TestNcrRecordFactory.minimalFollowupRecord().copy(
            patientCharacteristics = TestNcrRecordFactory.minimalDiagnosisRecord().patientCharacteristics.copy(
                vitStat = null,
                vitStatInt = null
            )
        )
        val entry = mapOf(1 to listOf(record)).entries.first()
        assertThat(filter.hasEmptyVitalStatusForVerbRecords(entry)).isTrue()
    }

    @Test
    fun `Should return false when any VERB record has vital status`() {
        val record = TestNcrRecordFactory.minimalFollowupRecord().copy(
            patientCharacteristics = TestNcrRecordFactory.minimalDiagnosisRecord().patientCharacteristics.copy(
                vitStat = 1,
                vitStatInt = 1
            )
        )        
        val entry = mapOf(1 to listOf(record)).entries.first()
        assertThat(filter.hasEmptyVitalStatusForVerbRecords(entry)).isFalse()
    }

    @Test
    fun `Should return true when all records have identical year of incidence`() {
        val record1 = TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            primaryDiagnosis = TestNcrRecordFactory.minimalDiagnosisRecord().primaryDiagnosis.copy(
                incjr = 2020
            )
        )
        val record2 = TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            primaryDiagnosis = TestNcrRecordFactory.minimalDiagnosisRecord().primaryDiagnosis.copy(
                incjr = 2020
            )
        )
        val entry = mapOf(1 to listOf(record1, record2)).entries.first()
        assertThat(filter.hasIdenticalYearOfIncidence(entry)).isTrue()
    }

    @Test
    fun `Should return false when records have different year of incidence`() {
        val record1 = TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            primaryDiagnosis = TestNcrRecordFactory.minimalDiagnosisRecord().primaryDiagnosis.copy(
                incjr = 2020
            )
        )
        val record2 = TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            primaryDiagnosis = TestNcrRecordFactory.minimalDiagnosisRecord().primaryDiagnosis.copy(
                incjr = 2021
            )
        )
        val entry = mapOf(1 to listOf(record1, record2)).entries.first()
        assertThat(filter.hasIdenticalYearOfIncidence(entry)).isFalse()
    }
}
