package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.DIAGNOSIS_EPISODE
import io.github.oshai.kotlinlogging.KotlinLogging

interface RecordFilter {
    val logFilteredRecords: Boolean
    
    private val logger get() = KotlinLogging.logger {}

    fun log(message: String) {
        if (logFilteredRecords) {
            logger.warn { message }
        }
    }

    fun apply(tumorRecords: List<NcrRecord>): Boolean
}

fun List<NcrRecord>.tumorId() = first().identification.keyZid

fun splitDiagnosisAndFollowup(records: List<NcrRecord>): Pair<List<NcrRecord>, List<NcrRecord>> {
    return records.partition { it.identification.epis == DIAGNOSIS_EPISODE }
}

fun Int?.notZeroNorNull(): Boolean {
    return this != null && this != 0
}

fun Int?.zeroOrNull(): Boolean {
    return this == null || this == 0
}
