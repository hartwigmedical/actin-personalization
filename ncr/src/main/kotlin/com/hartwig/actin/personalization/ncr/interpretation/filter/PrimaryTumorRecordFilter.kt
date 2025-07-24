package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord

class PrimaryTumorRecordFilter {
   
    internal fun hasValidDoubleTumorData(tumorRecordsPerId: Map.Entry<Int, List<NcrRecord>>): Boolean {
        val (primaryDiagnosis, followupDiagnosis) = extractPrimaryDiagnosis(tumorRecordsPerId)
        
        return primaryDiagnosis.all { it.clinicalCharacteristics.dubbeltum != null } &&
                followupDiagnosis.all { it.clinicalCharacteristics.dubbeltum == 0 }
    }


    internal fun hasValidMorfCatData(tumorRecordsPerId: Map.Entry<Int, List<NcrRecord>>): Boolean {
        val (primaryDiagnosis, followupDiagnosis) = extractPrimaryDiagnosis(tumorRecordsPerId)

        return primaryDiagnosis.all { it.primaryDiagnosis.morfCat != null } &&
                followupDiagnosis.all { it.primaryDiagnosis.morfCat == null }
    }

    internal fun hasValidAnusAfstData(tumorRecordsPerId: Map.Entry<Int, List<NcrRecord>>): Boolean {
        val (primaryDiagnosis, followupDiagnosis) = extractPrimaryDiagnosis(tumorRecordsPerId)

        // TODO: check if anusAfsf need to be non null in primary diagnosis
        return primaryDiagnosis.all { it.clinicalCharacteristics.anusAfst != null } &&
                followupDiagnosis.all { it.clinicalCharacteristics.anusAfst == null }
    }
    
    internal fun hasConsistentTopoSublokData(tumorRecordsPerId: Map.Entry<Int, List<NcrRecord>>): Boolean {
        val allTopoSubLock = tumorRecordsPerId.value.map { it.primaryDiagnosis.topoSublok }
        return allTopoSubLock.toSet().size == 1
    }    
}