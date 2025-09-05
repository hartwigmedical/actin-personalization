package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord

class ValidAnalDistanceFilter(override val logFilteredRecords: Boolean) : RecordFilter {

    override fun apply(tumorRecords: List<NcrRecord>): Boolean {
        val (_, followup) = splitDiagnosisAndFollowup(tumorRecords)
        val hasValidAnusAfstData = followup.all { it.clinicalCharacteristics.anusAfst == null }

        if (!hasValidAnusAfstData) {
            log("Tumor ${tumorRecords.tumorId()} has invalid anusAfst data")
        }

        return hasValidAnusAfstData
    }
}