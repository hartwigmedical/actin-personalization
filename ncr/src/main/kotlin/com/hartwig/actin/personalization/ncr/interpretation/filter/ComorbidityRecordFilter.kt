package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrCharlsonComorbidities
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.FOLLOW_UP_EPISODE

class ComorbidityRecordFilter(override val logFilteredRecords: Boolean) : RecordFilter {
    private fun NcrCharlsonComorbidities.allFieldsAreNull(): Boolean {
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
        ).all { it == null }
    }
    
    private fun NcrCharlsonComorbidities.allFieldsAreNotNull(): Boolean {
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
        ).all { it != null }
    }

    internal fun hasNoComorbidityOnFollowUpEpisode(tumorRecords: List<NcrRecord>): Boolean {
        val followupDiagnosis = tumorRecords.filter { it.identification.epis == FOLLOW_UP_EPISODE }
        return followupDiagnosis.all { it.comorbidities.allFieldsAreNull() }
    }

    internal fun hasCompleteComorbidityData(tumorRecords: List<NcrRecord>): Boolean {
        val allComorbidityRecords = tumorRecords.map { it.comorbidities }
        return allComorbidityRecords.all { it.allFieldsAreNull() || it.allFieldsAreNotNull() }
    }

    override fun tumorRecords(record: List<NcrRecord>): Boolean {
        return listOf(
            ::hasNoComorbidityOnFollowUpEpisode,
            ::hasCompleteComorbidityData
        ).all { it(record) }
    }
}