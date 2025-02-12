package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureType

object NcrPfsMeasureTypeMapper : NcrIntCodeMapper<ProgressionMeasureType> {

    override fun resolve(code: Int): ProgressionMeasureType {
        return when (code) {
            0 -> ProgressionMeasureType.CENSOR
            1 -> ProgressionMeasureType.PROGRESSION
            2 -> ProgressionMeasureType.DEATH
            else -> throw IllegalArgumentException("Unknown PfsMeasureType code: $code")
        }
    }
}
