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


    internal fun hasValidMorfCatData(tumorRecords: List<NcrRecord>): Boolean {
        val (primaryDiagnosis,followUpDiagnosis) = extractPrimaryDiagnosis(tumorRecords)
        val hasValidMorfCatData = primaryDiagnosis.all { it.primaryDiagnosis.morfCat != null } &&
               followUpDiagnosis.all { it.primaryDiagnosis.morfCat == null }
        if (!hasValidMorfCatData) {
            log("Tumor ${tumorRecords.tumorId()} has invalid morfCat data")
        }
        return hasValidMorfCatData
    }

    internal fun hasValidAnusAfstData(tumorRecords: List<NcrRecord>): Boolean {
        val (primaryDiagnosis,followUpDiagnosis) = extractPrimaryDiagnosis(tumorRecords)
        val hasValidAnusAfstData = primaryDiagnosis.all { it.clinicalCharacteristics.anusAfst != null } &&
               followUpDiagnosis.all { it.clinicalCharacteristics.anusAfst == null }
        if (!hasValidAnusAfstData) {
            log("Tumor ${tumorRecords.tumorId()} has invalid anusAfst data")
        }
        return hasValidAnusAfstData
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
            ::hasValidMorfCatData,
            ::hasValidAnusAfstData,
            ::hasConsistentTopoSublokData
        ).all { it(tumorRecords) }
    }
}