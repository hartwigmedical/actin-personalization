package com.hartwig.actin.personalization.ncr.interpretation.mapping

import com.hartwig.actin.personalization.datamodel.diagnosis.LymphaticInvasionCategory

object NcrLymphaticInvasionCategoryMapper : NcrIntCodeMapper<LymphaticInvasionCategory?> {

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
