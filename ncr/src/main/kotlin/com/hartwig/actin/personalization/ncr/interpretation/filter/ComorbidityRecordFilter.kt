package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrCharlsonComorbidities
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.FOLLOW_UP_EPISODE

class ComorbidityRecordFilter(override val logFilteredRecords: Boolean) : RecordFilter {

    override fun apply(tumorRecords: List<NcrRecord>): Boolean {
        return hasValidComorbidityData(tumorRecords)
    }

    internal fun hasValidComorbidityData(tumorRecords: List<NcrRecord>): Boolean {
        val hasValidComorbidityData = tumorRecords.all {
            it.comorbidities.allFieldsAreNull() ||
                    (it.identification.epis != FOLLOW_UP_EPISODE && it.comorbidities.allFieldsAreNotNull())
        }
        if (!hasValidComorbidityData) {
            log("Tumor ${tumorRecords.tumorId()} has comorbidity on follow-up episode")
        }
        return hasValidComorbidityData
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