package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.METASTATIC_DETECTION_AT_PROGRESSION
import com.hartwig.actin.personalization.ncr.interpretation.METASTATIC_DETECTION_AT_START

class ConsistentMetastaticProgressionFilter(override val logFilteredRecords: Boolean) : RecordFilter {
    
    override fun apply(tumorRecords: List<NcrRecord>): Boolean {
        val metastaticRecords = tumorRecords.filter {
            it.identification.metaEpis == METASTATIC_DETECTION_AT_START ||
                    it.identification.metaEpis == METASTATIC_DETECTION_AT_PROGRESSION
        }
        val hasConsistentMetastaticProgression = metastaticRecords.all { record ->
            val allMetaProgression = with(record.metastaticDiagnosis) {
                listOf(
                    metaProg1, metaProg2, metaProg3, metaProg4, metaProg5,
                    metaProg6, metaProg7, metaProg8, metaProg9, metaProg10
                )
            }
            if (record.identification.metaEpis == METASTATIC_DETECTION_AT_START) {
                allMetaProgression.any { it.zeroOrNull() }
            } else {
                allMetaProgression.any { it.notZeroNorNull() }
            }
        }

        if (!hasConsistentMetastaticProgression) {
            log("Inconsistent metastatic progression data found in tumor ${tumorRecords.tumorId()}")
        }

        return hasConsistentMetastaticProgression
    }
}