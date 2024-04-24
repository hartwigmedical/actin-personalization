package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.AnorectalVergeDistanceCategory
import com.hartwig.actin.personalization.ncr.interpretation.NcrIntCodeMapper

object NcrAnorectalVergeDistanceCategoryMapper : NcrIntCodeMapper<AnorectalVergeDistanceCategory?> {

    override fun resolve(code: Int): AnorectalVergeDistanceCategory? {
        return when (code) {
            1 -> AnorectalVergeDistanceCategory.ZERO_TO_FIVE_CM
            2 -> AnorectalVergeDistanceCategory.FIVE_TO_TEN_CM
            3 -> AnorectalVergeDistanceCategory.TEN_TO_FIFTEEN_CM
            4 -> AnorectalVergeDistanceCategory.OVER_FIFTEEN_CM
            9 -> null
            else -> throw IllegalArgumentException("Unknown AnorectalVergeDistanceCategory code: $code")
        }
    }
}
