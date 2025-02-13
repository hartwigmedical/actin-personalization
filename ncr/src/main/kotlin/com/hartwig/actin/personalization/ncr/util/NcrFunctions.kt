package com.hartwig.actin.personalization.ncr.util

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord

const val DIAGNOSIS_EPISODE = "DIA"

object NcrFunctions {

    fun diagnosisRecord(records: List<NcrRecord>) : NcrRecord {
        return records.single { it.identification.epis == DIAGNOSIS_EPISODE }
    }
}