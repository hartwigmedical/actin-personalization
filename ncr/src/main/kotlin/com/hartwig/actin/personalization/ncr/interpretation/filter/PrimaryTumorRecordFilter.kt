package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord

class PrimaryTumorRecordFilter(override val logFilteredRecords: Boolean) : RecordFilter {

    override fun apply(tumorRecords: List<NcrRecord>): Boolean {
        return listOf(
            ::hasValidDoubleTumorData,
            ::hasValidMorfCatData,
            ::hasValidAnusAfstData
        ).all { it(tumorRecords) }
    }

    internal fun hasValidDoubleTumorData(tumorRecords: List<NcrRecord>): Boolean {
        val (diagnosis, followup) = splitDiagnosisAndFollowup(tumorRecords)
        val hasValidDoubleTumorData = diagnosis.all { it.clinicalCharacteristics.dubbeltum != null } &&
                followup.all { it.clinicalCharacteristics.dubbeltum == 0 }
        
        if (!hasValidDoubleTumorData) {
            log("Invalid double tumor data found for tumor ${tumorRecords.tumorId()}")
        }
        
        return hasValidDoubleTumorData
    }

    internal fun hasValidMorfCatData(tumorRecords: List<NcrRecord>): Boolean {
        val (diagnosis, followup) = splitDiagnosisAndFollowup(tumorRecords)
        val hasValidMorfCatData = diagnosis.all { it.primaryDiagnosis.morfCat != null } &&
                followup.all { it.primaryDiagnosis.morfCat == null }
        
        if (!hasValidMorfCatData) {
            log("Tumor ${tumorRecords.tumorId()} has invalid morfCat data")
        }
        
        return hasValidMorfCatData
    }

    internal fun hasValidAnusAfstData(tumorRecords: List<NcrRecord>): Boolean {
        val (_, followup) = splitDiagnosisAndFollowup(tumorRecords)
        val hasValidAnusAfstData = followup.all { it.clinicalCharacteristics.anusAfst == null }
        
        if (!hasValidAnusAfstData) {
            log("Tumor ${tumorRecords.tumorId()} has invalid anusAfst data")
        }
        
        return hasValidAnusAfstData
    }
}