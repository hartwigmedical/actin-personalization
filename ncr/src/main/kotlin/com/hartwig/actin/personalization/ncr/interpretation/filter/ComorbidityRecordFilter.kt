package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.FOLLOW_UP_EPISODE

class ComorbidityRecordFilter(override val logFilteredRecords: Boolean) : RecordFilter {
    internal fun hasOnlyOneComorbidity(tumorRecords: List<NcrRecord>): Boolean {
        val followupDiagnosis = tumorRecords.filter { it.identification.epis == FOLLOW_UP_EPISODE }
        return followupDiagnosis.all { areAllFieldsNull(it.comorbidities) }
    }
    
    internal fun hasCompleteComorbidityData(tumorRecords: List<NcrRecord>): Boolean {
        val allComorbidityRecords = tumorRecords.map { it.comorbidities }
        return allComorbidityRecords.all { areAllFieldsNull(it) || areAllFieldsNotNull(it) }
    }

    override fun tumorRecords(record: List<NcrRecord>): Boolean {
        return listOf(
            ::hasOnlyOneComorbidity,
            ::hasCompleteComorbidityData
        ).all { it(record) }
    }
}