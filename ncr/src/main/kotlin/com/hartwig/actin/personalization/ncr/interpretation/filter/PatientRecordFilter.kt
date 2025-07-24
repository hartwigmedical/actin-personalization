package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.DIAGNOSIS_EPISODE
import com.hartwig.actin.personalization.ncr.interpretation.FOLLOW_UP_EPISODE
import io.github.oshai.kotlinlogging.KotlinLogging

class PatientRecordFilter(val log: (String) -> Unit) {
    internal fun hasValidTreatmentData(tumorRecordsPerId: Map.Entry<Int, List<NcrRecord>>): Boolean {
        val allTreatments = tumorRecordsPerId.value.map { it.treatment }

        val hasAtLeastOneInvalidTreatment = allTreatments.any { treatment ->
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

        if (hasAtLeastOneInvalidTreatment) {
            log("Invalid treatment data found for set of NCR tumor records with ID: ${tumorRecordsPerId.key}")
        }

        return !hasAtLeastOneInvalidTreatment
    }

    internal fun hasIdentialSex(tumorRecordsPerId: Map.Entry<Int, List<NcrRecord>>): Boolean {
        val allSexes = tumorRecordsPerId.value.map { it.patientCharacteristics.gesl }
        return allSexes.toSet().size == 1
    }

    internal fun hasExactlyOneDiagnosis(tumorRecordsPerId: Map.Entry<Int, List<NcrRecord>>): Boolean {
        val diagnosisRecords = tumorRecordsPerId.value.filter { it.identification.epis == DIAGNOSIS_EPISODE }
        if (diagnosisRecords.size != 1) {
            log("Expected exactly one diagnosis record for tumor ID ${tumorRecordsPerId.key}, found ${diagnosisRecords.size}")
            return false
        }
        return true
    }

    internal fun hasVitalStatusForDIARecords(tumorRecordsPerId: Map.Entry<Int, List<NcrRecord>>): Boolean {
        val diagnosisRecords = tumorRecordsPerId.value.filter { it.identification.epis == DIAGNOSIS_EPISODE }

        val hasVitalStatus =
            diagnosisRecords.all { it.patientCharacteristics.vitStat != null && it.patientCharacteristics.vitStatInt != null }
        if (!hasVitalStatus) {
            log("Missing vital status for diagnosis records of tumor ID ${tumorRecordsPerId.key}")
        }
        return hasVitalStatus
    }

    internal fun hasEmptyVitalStatusForVerbRecords(tumorRecordsPerId: Map.Entry<Int, List<NcrRecord>>): Boolean {
        val verbRecords = tumorRecordsPerId.value.filter { it.identification.epis == FOLLOW_UP_EPISODE }

        val hasEmptyVitalStatus =
            verbRecords.all { it.patientCharacteristics.vitStat == null && it.patientCharacteristics.vitStatInt == null }
        if (!hasEmptyVitalStatus) {
            log("Non-empty vital status found for verb records of tumor ID ${tumorRecordsPerId.key}")
        }
        return hasEmptyVitalStatus
    }

    internal fun hasIdenticalYearOfIncidence(tumorRecordsPerId: Map.Entry<Int, List<NcrRecord>>): Boolean {
        val yearsOfIncidence = tumorRecordsPerId.value.map { it.primaryDiagnosis.incjr }

        if (yearsOfIncidence.toSet().size > 1) {
            log("Multiple years of incidence found for tumor ID ${tumorRecordsPerId.key}")
            return false
        }
        return true
    }
}