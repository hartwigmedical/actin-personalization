package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.DIAGNOSIS_EPISODE

class ExactlyOnePrimaryDiagnosisRecordFilter(override val logFilteredRecords: Boolean) : RecordFilter {

    override fun apply(tumorRecords: List<NcrRecord>): Boolean {
        val numDiagnosisRecords = tumorRecords.count { it.identification.epis == DIAGNOSIS_EPISODE }
        val exactlyOneDiagnosis = numDiagnosisRecords == 1
        
        if (!exactlyOneDiagnosis) {
            log("Expected exactly one diagnosis record for tumor ID ${tumorRecords.tumorId()}, found $numDiagnosisRecords")
        }
        
        return exactlyOneDiagnosis
    }
}