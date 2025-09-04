package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.METASTATIC_DETECTION_AT_PROGRESSION
import com.hartwig.actin.personalization.ncr.interpretation.METASTATIC_DETECTION_AT_START

class MaxOneMetastaticDiagnosisFilter(override val logFilteredRecords: Boolean) : RecordFilter {
    
    override fun apply(tumorRecords: List<NcrRecord>): Boolean {
        val metastaticRecords = tumorRecords.filter {
            it.identification.metaEpis == METASTATIC_DETECTION_AT_START ||
                    it.identification.metaEpis == METASTATIC_DETECTION_AT_PROGRESSION
        }
        val hasAtMostOneMetastaticDetection = metastaticRecords.size <= 1

        if (!hasAtMostOneMetastaticDetection) {
            log("Multiple metastatic records found in tumor ${tumorRecords.tumorId()}")
        }

        return hasAtMostOneMetastaticDetection
    }
}