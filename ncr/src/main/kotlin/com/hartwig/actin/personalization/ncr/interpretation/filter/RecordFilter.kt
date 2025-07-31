package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.DIAGNOSIS_EPISODE
import com.hartwig.actin.personalization.ncr.interpretation.FOLLOW_UP_EPISODE
import io.github.oshai.kotlinlogging.KotlinLogging

interface RecordFilter {
    val logFilteredRecords: Boolean
    private val logger get() = KotlinLogging.logger {}

    fun log(message: String) {
        if (logFilteredRecords) {
            logger.info { message }
        }
    }

    fun extractPrimaryDiagnosis(records: List<NcrRecord>): Pair<List<NcrRecord>, List<NcrRecord>> {
        return records.filter { it.identification.epis == DIAGNOSIS_EPISODE } to
                records.filter { it.identification.epis == FOLLOW_UP_EPISODE }
    }

    fun List<NcrRecord>.tumorId() = first().identification.keyZid

    fun Int?.notZeroNorNull(): Boolean {
        return this != null && this != 0
    }
    fun Int?.ZeroOrNull(): Boolean {
        return this == null || this == 0
    }

    fun apply(tumorRecords: List<NcrRecord>): Boolean
}