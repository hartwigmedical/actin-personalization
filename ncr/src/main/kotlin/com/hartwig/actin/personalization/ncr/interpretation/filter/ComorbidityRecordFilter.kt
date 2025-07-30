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
        val hasNoComorbidityOnFollowUpEpisode = followupDiagnosis.all { it.comorbidities.allFieldsAreNull() }
        if (!hasNoComorbidityOnFollowUpEpisode) {
            log("Tumor ${tumorRecords.tumorId()} has comorbidity on follow-up episode")
        }
        return hasNoComorbidityOnFollowUpEpisode
    }

    internal fun hasCompleteComorbidityData(tumorRecords: List<NcrRecord>): Boolean {
        val allComorbidityRecords = tumorRecords.map { it.comorbidities }
        val hasCompleteComorbidityData = allComorbidityRecords.all { it.allFieldsAreNull() || it.allFieldsAreNotNull() }
        if (!hasCompleteComorbidityData) {
            log("Tumor ${tumorRecords.tumorId()} has incomplete comorbidity data")
        }
        return hasCompleteComorbidityData
    }

    override fun tumorRecords(record: List<NcrRecord>): Boolean {
        return listOf(
            ::hasNoComorbidityOnFollowUpEpisode,
            ::hasCompleteComorbidityData
        ).all { it(record) }
    }
}