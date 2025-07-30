package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.DIAGNOSIS_EPISODE
import com.hartwig.actin.personalization.ncr.interpretation.FOLLOW_UP_EPISODE
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.full.memberProperties

interface RecordFilter {
    val logFilteredRecords: Boolean

    fun log(message: String) {
        if (logFilteredRecords) {
            val logger = KotlinLogging.logger {}
            logger.info { message }
        }
    }

    fun extractPrimaryDiagnosis(records: List<NcrRecord>): Pair<List<NcrRecord>, List<NcrRecord>> {
        return records.filter { it.identification.epis == DIAGNOSIS_EPISODE } to
                records.filter { it.identification.epis == FOLLOW_UP_EPISODE }
    }

    fun List<NcrRecord>.tumorId() = first().identification.keyZid

    fun Int?.isNotZeroOrNull(): Boolean {
    return this != null && this != 0
}


    fun tumorRecords(record: List<NcrRecord>): Boolean
}

