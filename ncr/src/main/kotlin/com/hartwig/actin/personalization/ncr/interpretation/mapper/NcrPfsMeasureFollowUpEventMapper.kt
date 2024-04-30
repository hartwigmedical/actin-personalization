package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.PfsMeasureFollowUpEvent

object NcrPfsMeasureFollowUpEventMapper : NcrIntCodeMapper<PfsMeasureFollowUpEvent?> {

    override fun resolve(code: Int): PfsMeasureFollowUpEvent? {
        return when (code) {
            1 -> PfsMeasureFollowUpEvent.LOCAL_ONLY
            2 -> PfsMeasureFollowUpEvent.REGIONAL
            3 -> PfsMeasureFollowUpEvent.DISTANT_AND_POSSIBLY_REGIONAL_OR_LOCAL
            9 -> null
            else -> throw IllegalArgumentException("Unknown PfsMeasureFollowUpEvent code: $code")
        }
    }
}
