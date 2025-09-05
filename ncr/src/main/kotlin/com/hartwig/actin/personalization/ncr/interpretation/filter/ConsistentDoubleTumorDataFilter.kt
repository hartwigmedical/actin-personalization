package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord

class ConsistentDoubleTumorDataFilter(override val logFilteredRecords: Boolean) : RecordFilter {

    override fun apply(tumorRecords: List<NcrRecord>): Boolean {
        val (diagnosis, followup) = splitDiagnosisAndFollowup(tumorRecords)
        val hasValidDoubleTumorData = diagnosis.all { it.clinicalCharacteristics.dubbeltum != null } &&
                followup.all { it.clinicalCharacteristics.dubbeltum == 0 }

        if (!hasValidDoubleTumorData) {
            log("Invalid double tumor data found for tumor ${tumorRecords.tumorId()}")
        }

        return hasValidDoubleTumorData
    }
}