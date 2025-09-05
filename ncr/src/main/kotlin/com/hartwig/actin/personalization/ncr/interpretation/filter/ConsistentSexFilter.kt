package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord

class ConsistentSexFilter(override val logFilteredRecords: Boolean) : RecordFilter {

    override fun apply(tumorRecords: List<NcrRecord>): Boolean {
        val consistentSex = tumorRecords.map { it.patientCharacteristics.gesl }.toSet().size == 1

        if (!consistentSex) {
            log("Inconsistent sex found for tumor ID ${tumorRecords.tumorId()}")
        }
        return consistentSex
    }
}