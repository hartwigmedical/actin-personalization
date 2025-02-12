package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureFollowUpEvent

object NcrPfsMeasureFollowUpEventMapper : NcrIntCodeMapper<ProgressionMeasureFollowUpEvent?> {

    override fun resolve(code: Int): ProgressionMeasureFollowUpEvent? {
        return when (code) {
            1 -> ProgressionMeasureFollowUpEvent.LOCAL_ONLY
            2 -> ProgressionMeasureFollowUpEvent.REGIONAL
            3 -> ProgressionMeasureFollowUpEvent.DISTANT_AND_POSSIBLY_REGIONAL_OR_LOCAL
            9 -> null
            else -> throw IllegalArgumentException("Unknown PfsMeasureFollowUpEvent code: $code")
        }
    }
}
