package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import io.github.oshai.kotlinlogging.KotlinLogging

interface RecordFilter {
    val logFilteredRecords: Boolean
    
    fun log(message: String) {
        if (logFilteredRecords) {
            val logger = KotlinLogging.logger {}
            logger.info { message }
        }
    }
    
    fun List<NcrRecord>.tumorId() = first().identification.keyZid
    
    fun tumorRecords(record: List<NcrRecord>): Boolean
}