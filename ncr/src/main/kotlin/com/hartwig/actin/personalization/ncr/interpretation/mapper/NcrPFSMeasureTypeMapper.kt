package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.PFSMeasureType

object NcrPFSMeasureTypeMapper : NcrCodeMapper<PFSMeasureType> {

    override fun resolve(code: Int): PFSMeasureType {
        return when (code) {
            0 -> PFSMeasureType.CENSOR
            1 -> PFSMeasureType.PROGRESSION
            2 -> PFSMeasureType.DEATH
            else -> throw IllegalArgumentException("Unknown PFSMeasureType code: $code")
        }
    }
}
