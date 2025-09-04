package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrCharlsonComorbidities
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.FOLLOWUP_EPISODE

class ConsistentComorbidityDataFilter(override val logFilteredRecords: Boolean) : RecordFilter {

    override fun apply(tumorRecords: List<NcrRecord>): Boolean {
        val hasConsistentComorbidityData = tumorRecords.all {
            it.comorbidities.allFieldsAreNull() ||
                    (it.identification.epis != FOLLOWUP_EPISODE && it.comorbidities.allFieldsAreNotNull())
        }

        if (!hasConsistentComorbidityData) {
            log("Tumor ${tumorRecords.tumorId()} has comorbidity on follow-up episode")
        }

        return hasConsistentComorbidityData
    }
    
    private fun NcrCharlsonComorbidities.allFieldsAreNull(): Boolean {
        return allFields().all { it == null }
    }

    private fun NcrCharlsonComorbidities.allFieldsAreNotNull(): Boolean {
        return allFields().all { it != null }
    }

    private fun NcrCharlsonComorbidities.allFields(): List<Any?> {
        return listOf(
            cci,
            cciAids,
            cciCat,
            cciChf,
            cciCollagenosis,
            cciCopd,
            cciCvd,
            cciDementia,
            cciDm,
            cciEodDm,
            cciMalignancy,
            cciMetastatic,
            cciMi,
            cciMildLiver,
            cciPlegia,
            cciPvd,
            cciRenal,
            cciSevereLiver,
            cciUlcer
        )
    }
}