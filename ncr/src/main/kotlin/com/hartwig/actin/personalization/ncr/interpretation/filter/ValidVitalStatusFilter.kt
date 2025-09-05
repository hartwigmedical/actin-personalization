package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.DIAGNOSIS_EPISODE
import com.hartwig.actin.personalization.ncr.interpretation.FOLLOWUP_EPISODE

class ValidVitalStatusFilter(override val logFilteredRecords: Boolean) : RecordFilter {

    override fun apply(tumorRecords: List<NcrRecord>): Boolean {
        val diagnosisRecords = tumorRecords.filter { it.identification.epis == DIAGNOSIS_EPISODE }

        val hasPopulatedDiagnosisVitalStatus =
            diagnosisRecords.all { it.patientCharacteristics.vitStat != null && it.patientCharacteristics.vitStatInt != null }

        if (!hasPopulatedDiagnosisVitalStatus) {
            log("Missing vital status for diagnosis records of tumor ID ${tumorRecords.tumorId()}")
        }

        val followupRecords = tumorRecords.filter { it.identification.epis == FOLLOWUP_EPISODE }

        val hasEmptyFollowupVitalStatus =
            followupRecords.all { it.patientCharacteristics.vitStat == null && it.patientCharacteristics.vitStatInt == null }

        if (!hasEmptyFollowupVitalStatus) {
            log("Non-empty vital status found for verb records of tumor ID ${tumorRecords.tumorId()}")
        }

        return hasPopulatedDiagnosisVitalStatus && hasEmptyFollowupVitalStatus
    }
}