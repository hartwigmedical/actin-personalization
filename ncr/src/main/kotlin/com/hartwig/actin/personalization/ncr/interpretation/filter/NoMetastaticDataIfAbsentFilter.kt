package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.METASTATIC_DETECTION_ABSENT

class NoMetastaticDataIfAbsentFilter(override val logFilteredRecords: Boolean) : RecordFilter {
    
    override fun apply(tumorRecords: List<NcrRecord>): Boolean {
        val absentMetastaticRecords = tumorRecords.filter { it.identification.metaEpis == METASTATIC_DETECTION_ABSENT }
        val hasEmptyMetastaticFieldIfDetectionNotPresent = absentMetastaticRecords.all { record ->
            val hasEmptyMetastaticDiagnosis = with(record.metastaticDiagnosis) {
                listOf(
                    metaProg1, metaProg2, metaProg3, metaProg4, metaProg5,
                    metaProg6, metaProg7, metaProg8, metaProg9, metaProg10
                )
            }.all { it == null }
            val hasEmptyMetastaticSurgery = record.treatment.metastaticSurgery.metaChirCode1 == null &&
                    record.treatment.metastaticSurgery.metaChirCode2 == null &&
                    record.treatment.metastaticSurgery.metaChirCode3 == null
            val hasEmptyMetastaticRadiotherapy = record.treatment.metastaticRadiotherapy.metaRtCode1 == null &&
                    record.treatment.metastaticRadiotherapy.metaRtCode2 == null &&
                    record.treatment.metastaticRadiotherapy.metaRtCode3 == null &&
                    record.treatment.metastaticRadiotherapy.metaRtCode4 == null
            hasEmptyMetastaticDiagnosis && hasEmptyMetastaticSurgery && hasEmptyMetastaticRadiotherapy
        }

        if (!hasEmptyMetastaticFieldIfDetectionNotPresent) {
            log("Non-empty metastatic fields found in tumor ${tumorRecords.tumorId()} without detection")
        }

        return hasEmptyMetastaticFieldIfDetectionNotPresent
    }

}