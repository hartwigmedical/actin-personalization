package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord

interface RecordFilter {
    val logFilteredRecords: Boolean
    
    fun log(message: String) {
        if (logFilteredRecords) {
            println(message)
        }
    }
    
    fun apply(record: List<NcrRecord>): Boolean
}