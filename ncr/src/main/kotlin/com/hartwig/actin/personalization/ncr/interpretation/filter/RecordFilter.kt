package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import io.github.oshai.kotlinlogging.KotlinLogging

interface RecordFilter {
    val logFilteredRecords: Boolean
    private val logger get() = KotlinLogging.logger {}
    
    fun log(message: String) {
        if (logFilteredRecords) {
            logger.info { message }
        }
    }
    
    fun List<NcrRecord>.tumorId() = first().identification.keyZid
    
    fun apply(tumorRecords: List<NcrRecord>): Boolean
}