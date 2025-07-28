package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import kotlin.reflect.full.memberProperties

class MetastaticDiagnosisRecordFilter(override val logFilteredRecords: Boolean) : RecordFilter {
    internal fun hasOnlyOneMetastaticDetection(records: List<NcrRecord>): Boolean {
        val metastaticRecords =
            records.filter { it.identification.metaEpis == 1 || it.identification.metaEpis == 2 }
        return metastaticRecords.size <= 1
    }

    internal fun hasEmptyMetastaticFieldIfDetectionNotPresent(records: List<NcrRecord>): Boolean {
        val absentMetastaticRecords = records.filter { it.identification.metaEpis == 0 }
        return absentMetastaticRecords.all {
            listOf(
                it.metastaticDiagnosis,
                it.treatment.metastaticSurgery,
                it.treatment.metastaticRadiotherapy
            ).all { areAllFieldsNull(it) }
        }
    }

    override fun tumorRecords(record: List<NcrRecord>): Boolean {
        return listOf(
            ::hasOnlyOneMetastaticDetection,
            ::hasEmptyMetastaticFieldIfDetectionNotPresent,
        ).all { it(record) }
    }
}