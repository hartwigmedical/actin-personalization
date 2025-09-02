package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord

class PrimaryTumorRecordFilter(override val logFilteredRecords: Boolean) : RecordFilter {
   
    internal fun hasValidDoubleTumorData(tumorRecords: List<NcrRecord>): Boolean {
        val (primaryDiagnosis,followUpDiagnosis) = extractPrimaryDiagnosis(tumorRecords)
        val hasValidDoubleTumorData = primaryDiagnosis.all { it.clinicalCharacteristics.dubbeltum != null } &&
               followUpDiagnosis.all { it.clinicalCharacteristics.dubbeltum == 0 }
        if (!hasValidDoubleTumorData) {
            log("Tumor ${tumorRecords.tumorId()} has invalid double tumor data")
        }
        return hasValidDoubleTumorData
    }

    
    internal fun hasConsistentTopoSublokData(tumorRecords: List<NcrRecord>): Boolean {
        val allTopoSubLock = tumorRecords.map { it.primaryDiagnosis.topoSublok }
        val hasConsistentTopoSublokData = allTopoSubLock.toSet().size == 1
        if (!hasConsistentTopoSublokData) {
            log("Tumor ${tumorRecords.tumorId()} has inconsistent topoSublok data: $allTopoSubLock")
        }
        return hasConsistentTopoSublokData
    }

    override fun apply(tumorRecords: List<NcrRecord>): Boolean {
        return listOf(
            ::hasValidDoubleTumorData,
            ::hasConsistentTopoSublokData
        ).all { it(tumorRecords) }
    }
}