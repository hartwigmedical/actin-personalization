package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.ExtraMuralInvasionCategory

object NcrExtraMuralInvasionCategoryMapper : NcrIntCodeMapper<ExtraMuralInvasionCategory?> {

    override fun resolve(code: Int): ExtraMuralInvasionCategory? {
        return when (code) {
            0 -> ExtraMuralInvasionCategory.LESS_THAN_FIVE_MM
            1 -> ExtraMuralInvasionCategory.ABOVE_FIVE_MM
            8 -> ExtraMuralInvasionCategory.NA
            9 -> null
            else -> throw IllegalArgumentException("Unknown ExtraMuralInvasionCategory code: $code")
        }
    }
}
