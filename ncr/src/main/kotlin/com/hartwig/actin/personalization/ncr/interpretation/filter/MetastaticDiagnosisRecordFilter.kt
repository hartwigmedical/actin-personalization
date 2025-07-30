package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord

class MetastaticDiagnosisRecordFilter(override val logFilteredRecords: Boolean) : RecordFilter {
    internal fun hasOnlyOneMetastaticDetection(records: List<NcrRecord>): Boolean {
        val metastaticRecords =
            records.filter { it.identification.metaEpis == 1 || it.identification.metaEpis == 2 }
        val hasOnlyOneMetastaticDetection = metastaticRecords.size <= 1
        if (!hasOnlyOneMetastaticDetection) {
            log("Multiple metastatic detections found in tumor ${records.tumorId()}")
        }
        return hasOnlyOneMetastaticDetection
    }

    internal fun hasEmptyMetastaticFieldIfDetectionNotPresent(records: List<NcrRecord>): Boolean {
        val absentMetastaticRecords = records.filter { it.identification.metaEpis == 0 }
        val hasEmptyMetastaticFieldIfDetectionNotPresent = absentMetastaticRecords.all { record ->
            val hasEmptyMetastaticDiagnosis = record.metastaticDiagnosis.metaProg1 == null &&
                    record.metastaticDiagnosis.metaProg2 == null &&
                    record.metastaticDiagnosis.metaProg3 == null &&
                    record.metastaticDiagnosis.metaProg4 == null &&
                    record.metastaticDiagnosis.metaProg5 == null &&
                    record.metastaticDiagnosis.metaProg6 == null &&
                    record.metastaticDiagnosis.metaProg7 == null &&
                    record.metastaticDiagnosis.metaProg8 == null &&
                    record.metastaticDiagnosis.metaProg9 == null &&
                    record.metastaticDiagnosis.metaProg10 == null
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
            log("Non-empty metastatic fields found in tumor ${records.tumorId()} without detection")
        }
        return hasEmptyMetastaticFieldIfDetectionNotPresent
    }

    internal fun hasConsistentMetastaticProgression(records: List<NcrRecord>): Boolean {
         val hasConsistentMetastaticProgression = records.all { record ->
            val allMetaProgression = listOf(
                record.metastaticDiagnosis.metaProg1,
                record.metastaticDiagnosis.metaProg2,
                record.metastaticDiagnosis.metaProg3,
                record.metastaticDiagnosis.metaProg4,
                record.metastaticDiagnosis.metaProg5,
                record.metastaticDiagnosis.metaProg6,
                record.metastaticDiagnosis.metaProg7,
                record.metastaticDiagnosis.metaProg8,
                record.metastaticDiagnosis.metaProg9,
                record.metastaticDiagnosis.metaProg10
            )
            when (record.identification.metaEpis) {
                0 -> allMetaProgression.all { !it.notZeroNorNull() }
                1 -> allMetaProgression.all { !it.notZeroNorNull() }
                2 -> allMetaProgression.any { it.notZeroNorNull() }
                else -> false
            }
        }
        
        if (!hasConsistentMetastaticProgression) {
            log("Inconsistent metastatic progression data found in tumor ${records.tumorId()}")
        }
        return hasConsistentMetastaticProgression
    }

    override fun apply(tumorRecords: List<NcrRecord>): Boolean {
        return listOf(
            ::hasOnlyOneMetastaticDetection,
            ::hasEmptyMetastaticFieldIfDetectionNotPresent,
            ::hasConsistentMetastaticProgression,
        ).all { it(tumorRecords) }
    }
}