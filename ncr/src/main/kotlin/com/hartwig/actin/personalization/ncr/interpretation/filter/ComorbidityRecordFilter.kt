package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.FOLLOW_UP_EPISODE

class ComorbidityRecordFilter {
    internal fun hasOnlyOneComorbidity(tumorRecordsPerId: Map.Entry<Int, List<NcrRecord>>): Boolean {
        val followupDiagnosis = tumorRecordsPerId.value.filter { it.identification.epis == FOLLOW_UP_EPISODE }
        return followupDiagnosis.all { areAllFieldsNull(it.comorbidities) }
    }
    
    internal fun hasCompleteComorbidityData(tumorRecordsPerId: Map.Entry<Int, List<NcrRecord>>): Boolean {
        val allComorbidityRecords = tumorRecordsPerId.value.map { it.comorbidities }
        return allComorbidityRecords.all { areAllFieldsNull(it) || areAllFieldsNotNull(it) }
    }
}