package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.ncr.datamodel.VenousInvasionCategory

object NcrVenousInvasionCategoryMapper : NcrCodeMapper<VenousInvasionCategory?> {

    override fun resolve(code: Int): VenousInvasionCategory? {
        return when (code) {
            0 -> VenousInvasionCategory.NONE
            1 -> VenousInvasionCategory.EXTRAMURAL
            2 -> VenousInvasionCategory.INTRAMURAL
            5 -> VenousInvasionCategory.SUSPECT
            8 -> VenousInvasionCategory.NA
            9 -> null
            else -> throw IllegalArgumentException("Unknown VenousInvasionCategory code: $code")
        }
    }
}
