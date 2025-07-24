package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.DIAGNOSIS_EPISODE
import com.hartwig.actin.personalization.ncr.interpretation.FOLLOW_UP_EPISODE
import kotlin.reflect.full.memberProperties

inline fun <reified T : Any> areAllFieldsNull(obj: T): Boolean {
    return T::class.memberProperties.all { prop ->
        prop.get(obj) == null
    }
}

inline fun <reified T : Any> areAllFieldsNotNull(obj: T): Boolean {
    return T::class.memberProperties.all { prop ->
        prop.get(obj) != null
    }
}


internal fun extractPrimaryDiagnosis(tumorRecordsPerId: Map.Entry<Int, List<NcrRecord>>): Pair<List<NcrRecord>, List<NcrRecord>> {
    return tumorRecordsPerId.value.filter { it.identification.epis == DIAGNOSIS_EPISODE } to
            tumorRecordsPerId.value.filter { it.identification.epis == FOLLOW_UP_EPISODE }
}