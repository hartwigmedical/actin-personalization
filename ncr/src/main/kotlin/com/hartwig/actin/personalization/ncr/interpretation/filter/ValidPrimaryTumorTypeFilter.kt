package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord

class ValidPrimaryTumorTypeFilter(override val logFilteredRecords: Boolean) : RecordFilter {

    override fun apply(tumorRecords: List<NcrRecord>): Boolean {
        val (diagnosis, followup) = splitDiagnosisAndFollowup(tumorRecords)
        val hasValidMorfCatData = diagnosis.all { it.primaryDiagnosis.morfCat != null } &&
                followup.all { it.primaryDiagnosis.morfCat == null }

        if (!hasValidMorfCatData) {
            log("Tumor ${tumorRecords.tumorId()} has invalid morfCat data")
        }

        return hasValidMorfCatData
    }
}