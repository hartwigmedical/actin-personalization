package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord

class PrimaryTumorRecordFilter(override val logFilteredRecords: Boolean) : RecordFilter {
   
    internal fun hasValidDoubleTumorData(tumorRecords: List<NcrRecord>): Boolean {
        val (primaryDiagnosis, followupDiagnosis) = extractPrimaryDiagnosis(tumorRecords)
        
        return primaryDiagnosis.all { it.clinicalCharacteristics.dubbeltum != null } &&
                followupDiagnosis.all { it.clinicalCharacteristics.dubbeltum == 0 }
    }


    internal fun hasValidMorfCatData(tumorRecords: List<NcrRecord>): Boolean {
        val (primaryDiagnosis, followupDiagnosis) = extractPrimaryDiagnosis(tumorRecords)

        return primaryDiagnosis.all { it.primaryDiagnosis.morfCat != null } &&
                followupDiagnosis.all { it.primaryDiagnosis.morfCat == null }
    }

    internal fun hasValidAnusAfstData(tumorRecords: List<NcrRecord>): Boolean {
        val (primaryDiagnosis, followupDiagnosis) = extractPrimaryDiagnosis(tumorRecords)

        // TODO: check if anusAfsf need to be non null in primary diagnosis
        return primaryDiagnosis.all { it.clinicalCharacteristics.anusAfst != null } &&
                followupDiagnosis.all { it.clinicalCharacteristics.anusAfst == null }
    }
    
    internal fun hasConsistentTopoSublokData(tumorRecords: List<NcrRecord>): Boolean {
        val allTopoSubLock = tumorRecords.map { it.primaryDiagnosis.topoSublok }
        return allTopoSubLock.toSet().size == 1
    }

    override fun tumorRecords(record: List<NcrRecord>): Boolean {
        return listOf(
            ::hasValidDoubleTumorData,
            ::hasValidMorfCatData,
            ::hasValidAnusAfstData,
            ::hasConsistentTopoSublokData
        ).all { it(record) }
    }
}