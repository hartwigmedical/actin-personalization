package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.DIAGNOSIS_EPISODE
import com.hartwig.actin.personalization.ncr.interpretation.FOLLOWUP_EPISODE

class PatientRecordFilter(override val logFilteredRecords: Boolean) : RecordFilter {

    override fun apply(tumorRecords: List<NcrRecord>): Boolean {
        return listOf(
            ::hasConsistentTreatmentData,
            ::hasConsistentSex,
            ::hasExactlyOneDiagnosis,
            ::hasVitalStatusForDiagnosisRecords,
            ::hasEmptyVitalStatusForVerbRecords,
        ).all { it(tumorRecords) }
    }

    internal fun hasConsistentTreatmentData(tumorRecords: List<NcrRecord>): Boolean {
        val allTreatments = tumorRecords.map { it.treatment }
        val indicatesTreatmentButNoTreatmentDefined = allTreatments.any { treatment ->
            treatment.tumgerichtTher == 1 &&
                    (treatment.primarySurgery.chir == null || treatment.primarySurgery.chir == 0) &&
                    treatment.primaryRadiotherapy.rt == 0 &&
                    (treatment.primaryRadiotherapy.chemort == null || treatment.primaryRadiotherapy.chemort == 0) &&
                    (treatment.gastroenterologyResection.mdlRes == null || treatment.gastroenterologyResection.mdlRes == 0) &&
                    (treatment.hipec.hipec == null || treatment.hipec.hipec == 0) &&
                    treatment.systemicTreatment.chemo == 0 &&
                    treatment.systemicTreatment.target == 0 &&
                    treatment.metastaticSurgery.metaChirInt1 == null &&
                    treatment.metastaticRadiotherapy.metaRtCode1 == null
        }

        if (indicatesTreatmentButNoTreatmentDefined) {
            log("Inconsistent treatment data found for set of NCR tumor records with ID: ${tumorRecords.tumorId()}")
        }

        return !indicatesTreatmentButNoTreatmentDefined
    }

    internal fun hasConsistentSex(tumorRecords: List<NcrRecord>): Boolean {
        val consistentSex = tumorRecords.map { it.patientCharacteristics.gesl }.toSet().size == 1

        if (!consistentSex) {
            log("Inconsistent sex found for tumor ID ${tumorRecords.tumorId()}")
        }
        return consistentSex
    }

    internal fun hasExactlyOneDiagnosis(tumorRecords: List<NcrRecord>): Boolean {
        val numDiagnosisRecords = tumorRecords.count { it.identification.epis == DIAGNOSIS_EPISODE }
        val exactlyOneDiagnosis = numDiagnosisRecords == 1
        if (!exactlyOneDiagnosis) {
            log("Expected exactly one diagnosis record for tumor ID ${tumorRecords.tumorId()}, found ${numDiagnosisRecords}")
        }
        return exactlyOneDiagnosis
    }

    internal fun hasVitalStatusForDiagnosisRecords(tumorRecords: List<NcrRecord>): Boolean {
        val diagnosisRecords = tumorRecords.filter { it.identification.epis == DIAGNOSIS_EPISODE }

        val hasVitalStatus =
            diagnosisRecords.all { it.patientCharacteristics.vitStat != null && it.patientCharacteristics.vitStatInt != null }

        if (!hasVitalStatus) {
            log("Missing vital status for diagnosis records of tumor ID ${tumorRecords.tumorId()}")
        }

        return hasVitalStatus
    }

    internal fun hasEmptyVitalStatusForVerbRecords(tumorRecords: List<NcrRecord>): Boolean {
        val verbRecords = tumorRecords.filter { it.identification.epis == FOLLOWUP_EPISODE }

        val hasEmptyVitalStatus =
            verbRecords.all { it.patientCharacteristics.vitStat == null && it.patientCharacteristics.vitStatInt == null }
        
        if (!hasEmptyVitalStatus) {
            log("Non-empty vital status found for verb records of tumor ID ${tumorRecords.tumorId()}")
        }
        
        return hasEmptyVitalStatus
    }
}