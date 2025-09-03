package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PatientRecordFilterTest {
    
    private val filter = PatientRecordFilter(true)
    private val minimalDiagnosisRecord = TestNcrRecordFactory.minimalDiagnosisRecord()

    @Test
    fun `Should return true when treatment is valid`() {
        val records = listOf(minimalDiagnosisRecord)
        assertThat(filter.hasConsistentTreatmentData(records)).isTrue()
    }

    @Test
    fun `Should return false when treatment is invalid`() {
        val records = listOf(minimalDiagnosisRecord.copy(
            treatment = minimalDiagnosisRecord.treatment.copy(
                tumgerichtTher = 1,
                primarySurgery = minimalDiagnosisRecord.treatment.primarySurgery.copy(chir = null),
                primaryRadiotherapy = minimalDiagnosisRecord.treatment.primaryRadiotherapy.copy(
                    rt = 0,
                    chemort = 0
                ),
                gastroenterologyResection = minimalDiagnosisRecord.treatment.gastroenterologyResection.copy(
                    mdlRes = null
                ),
                hipec = minimalDiagnosisRecord.treatment.hipec.copy(hipec = null),
                systemicTreatment = minimalDiagnosisRecord.treatment.systemicTreatment.copy(
                    chemo = 0,
                    target = 0
                ),
                metastaticSurgery = minimalDiagnosisRecord.treatment.metastaticSurgery.copy(
                    metaChirInt1 = null
                ),
                metastaticRadiotherapy = minimalDiagnosisRecord.treatment.metastaticRadiotherapy.copy(
                    metaRtCode1 = null
                )
            )
        ))
        assertThat(filter.hasConsistentTreatmentData(records)).isFalse()
    }

    @Test
    fun `Should return true when all records have identical sex`() {
        val record1 = minimalDiagnosisRecord.copy(
            patientCharacteristics = minimalDiagnosisRecord.patientCharacteristics.copy(gesl = 1)
        )
        val record2 = record1.copy()
        val records = listOf(record1, record2)
        assertThat(filter.hasConsistentSex(records)).isTrue()
    }

    @Test
    fun `Should return false when records have different sex`() {
        val record1 = minimalDiagnosisRecord.copy(
            patientCharacteristics = minimalDiagnosisRecord.patientCharacteristics.copy(gesl = 1)
        )
        val record2 = record1.copy(patientCharacteristics = record1.patientCharacteristics.copy(gesl = 2))
        val records = listOf(record1, record2)
        assertThat(filter.hasConsistentSex(records)).isFalse()
    }

    @Test
    fun `Should return true when there is exactly one diagnosis`() {
        val records = listOf(minimalDiagnosisRecord)
        assertThat(filter.hasExactlyOneDiagnosis(records)).isTrue()
    }

    @Test
    fun `Should return false when there are zero or multiple diagnoses`() {
        val record = minimalDiagnosisRecord
        val followUp = record.copy(identification = record.identification.copy(epis = "NOT DIA"))
        val entryNone = listOf(followUp)
        val entryMultiple = listOf(record, record)
        assertThat(filter.hasExactlyOneDiagnosis(entryNone)).isFalse()
        assertThat(filter.hasExactlyOneDiagnosis(entryMultiple)).isFalse()
    }

    @Test
    fun `Should return true when all DIA records have vital status`() {
        val records = listOf(minimalDiagnosisRecord.copy(
            patientCharacteristics = minimalDiagnosisRecord.patientCharacteristics.copy(
                vitStat = 1,
                vitStatInt = 1
            )
        ))
        assertThat(filter.hasVitalStatusForDiagnosisRecords(records)).isTrue()
    }

    @Test
    fun `Should return false when any DIA record is missing vital status`() {
        val records = listOf(minimalDiagnosisRecord.copy(
            patientCharacteristics = minimalDiagnosisRecord.patientCharacteristics.copy(
                vitStat = null,
                vitStatInt = null
            )
        ))
        assertThat(filter.hasVitalStatusForDiagnosisRecords(records)).isFalse()
    }

    @Test
    fun `Should return true when all VERB records have empty vital status`() {
        val records = listOf(TestNcrRecordFactory.minimalFollowupRecord().copy(
            patientCharacteristics = minimalDiagnosisRecord.patientCharacteristics.copy(
                vitStat = null,
                vitStatInt = null
            )
        ))
        assertThat(filter.hasEmptyVitalStatusForVerbRecords(records)).isTrue()
    }

    @Test
    fun `Should return false when any VERB record has vital status`() {
        val records = listOf(TestNcrRecordFactory.minimalFollowupRecord().copy(
            patientCharacteristics = minimalDiagnosisRecord.patientCharacteristics.copy(
                vitStat = 1,
                vitStatInt = 1
            )
        ))        
        assertThat(filter.hasEmptyVitalStatusForVerbRecords(records)).isFalse()
    }
}
