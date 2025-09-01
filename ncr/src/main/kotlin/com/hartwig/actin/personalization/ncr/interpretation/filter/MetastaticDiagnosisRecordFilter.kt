package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord

class MetastaticDiagnosisRecordFilter(override val logFilteredRecords: Boolean) : RecordFilter {
    private val METASTATIC_DETECTION_ABSENT = 0
    private val METASTATIC_DETECTION_AT_START = 1
    private val METASTATIC_DETECTION_AT_PROGRESSION = 2

    internal fun hasAtLeastOneMetastaticDetection(records: List<NcrRecord>): Boolean {
        val hasAtLeastOneClinicalMetastaticDetection = records.any { it.primaryDiagnosis.cm != null && it.primaryDiagnosis.cm != "0" }
        val hasAtLeastOnePathologicalMetastaticDetection = records.any { it.primaryDiagnosis.pm != null && it.primaryDiagnosis.pm != "-" }
        val hasAtLeastOneMetastaticDetection = hasAtLeastOneClinicalMetastaticDetection || hasAtLeastOnePathologicalMetastaticDetection
        if (!hasAtLeastOneMetastaticDetection) {
            log("No metastatic detection found in tumor ${records.tumorId()}")
        }
        return hasAtLeastOneMetastaticDetection
    }
    
    internal fun hasAtMostOneMetastaticDetection(records: List<NcrRecord>): Boolean {
        val metastaticRecords =
            records.filter { it.identification.metaEpis == METASTATIC_DETECTION_AT_START || it.identification.metaEpis == METASTATIC_DETECTION_AT_PROGRESSION }
        val hasAtMostOneMetastaticDetection = metastaticRecords.size <= 1
        if (!hasAtMostOneMetastaticDetection) {
            log("Multiple metastatic detections found in tumor ${records.tumorId()}")
        }
        return hasAtMostOneMetastaticDetection
    }

    internal fun hasEmptyMetastaticFieldIfDetectionNotPresent(records: List<NcrRecord>): Boolean {
        val absentMetastaticRecords = records.filter { it.identification.metaEpis == METASTATIC_DETECTION_ABSENT }
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
            log("Non-empty metastatic fields found in tumor ${records.tumorId()} without detection")
        }
        return hasEmptyMetastaticFieldIfDetectionNotPresent
    }

    internal fun hasConsistentMetastaticProgression(records: List<NcrRecord>): Boolean {
        val metastaticRecords = records.filter {
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
                allMetaProgression.all { !it.notZeroNorNull() }
            } else {
                allMetaProgression.any { it.notZeroNorNull() }
            }
        }

        if (!hasConsistentMetastaticProgression) {
            log("Inconsistent metastatic progression data found in tumor ${records.tumorId()}")
        }
        return hasConsistentMetastaticProgression
    }

    override fun apply(tumorRecords: List<NcrRecord>): Boolean {
        return listOf(
            ::hasAtMostOneMetastaticDetection,
            ::hasEmptyMetastaticFieldIfDetectionNotPresent,
            ::hasConsistentMetastaticProgression,
            ::hasAtLeastOneMetastaticDetection,
        ).all { it(tumorRecords) }
    }
}