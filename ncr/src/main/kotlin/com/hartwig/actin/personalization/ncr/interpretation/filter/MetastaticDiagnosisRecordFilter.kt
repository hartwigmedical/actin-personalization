package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import kotlin.reflect.full.memberProperties

class MetastaticDiagnosisRecordFilter {

    // check if all fields are empty
    inline fun <reified T : Any> areAllFieldsNull(obj: T): Boolean {
        return T::class.memberProperties.all { prop ->
            prop.get(obj) == null
        }
    }

    internal fun hasOnlyOneMetastaticDetection(tumorRecordsPerId: Map.Entry<Int, List<NcrRecord>>): Boolean {
        val metastaticRecords =
            tumorRecordsPerId.value.filter { it.identification.metaEpis == 1 || it.identification.metaEpis == 2 }
        return metastaticRecords.size <= 1
    }

    internal fun hasEmptyMetastaticFieldIfDetectionNotPresent(tumorRecordsPerId: Map.Entry<Int, List<NcrRecord>>): Boolean {
        val absentMetastaticRecords = tumorRecordsPerId.value.filter { it.identification.metaEpis == 0 }
        return absentMetastaticRecords.all {
            listOf(
                it.metastaticDiagnosis,
                it.treatment.metastaticSurgery,
                it.treatment.metastaticRadiotherapy
            ).all { areAllFieldsNull(it) }
        }
    }
}