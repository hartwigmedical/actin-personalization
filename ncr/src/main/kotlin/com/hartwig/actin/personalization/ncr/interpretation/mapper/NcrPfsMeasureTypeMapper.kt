package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.PfsMeasureType
import com.hartwig.actin.personalization.ncr.interpretation.NcrIntCodeMapper

object NcrPfsMeasureTypeMapper : NcrIntCodeMapper<PfsMeasureType> {

    override fun resolve(code: Int): PfsMeasureType {
        return when (code) {
            0 -> PfsMeasureType.CENSOR
            1 -> PfsMeasureType.PROGRESSION
            2 -> PfsMeasureType.DEATH
            else -> throw IllegalArgumentException("Unknown PfsMeasureType code: $code")
        }
    }
}
