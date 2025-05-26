package com.hartwig.actin.personalization.ncr.interpretation.mapping

import com.hartwig.actin.personalization.datamodel.treatment.RadiotherapyType

object NcrRadiotherapyTypeMapper : NcrIntCodeMapper<RadiotherapyType?> {

    override fun resolve(code: Int): RadiotherapyType? {
        return when (code) {
            1 -> RadiotherapyType.CHEMORADIATION
            2 -> RadiotherapyType.SHORT_DURATION
            3 -> RadiotherapyType.LONG_DURATION
            4 -> RadiotherapyType.INTERNAL
            5 -> RadiotherapyType.INTRA_OPERATIVE
            6 -> null
            else -> throw IllegalArgumentException("Unknown RadiotherapyType code: $code")
        }
    }
}
