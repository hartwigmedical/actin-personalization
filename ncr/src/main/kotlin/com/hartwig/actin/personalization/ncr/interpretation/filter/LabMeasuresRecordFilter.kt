package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord

class LabMeasuresRecordFilter(override val logFilteredRecords: Boolean) : RecordFilter {
    
    internal fun hasCompleteLabMeasuresData(tumorRecords: List<NcrRecord>): Boolean {
        TODO()
    }

    override fun tumorRecords(record: List<NcrRecord>): Boolean {
        return hasCompleteLabMeasuresData(record)
    }
}