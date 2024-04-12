package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.LymphaticInvasionCategory

object NcrLymphaticInvasionCategoryMapper : NcrCodeMapper<LymphaticInvasionCategory?> {

    override fun resolve(code: Int): LymphaticInvasionCategory? {
        return when (code) {
            0 -> LymphaticInvasionCategory.NONE
            1 -> LymphaticInvasionCategory.PRESENT
            5 -> LymphaticInvasionCategory.SUSPECT
            8 -> LymphaticInvasionCategory.NA
            9 -> null
            else -> throw IllegalArgumentException("Unknown LymphaticInvasionCategory code: $code")
        }
    }
}
