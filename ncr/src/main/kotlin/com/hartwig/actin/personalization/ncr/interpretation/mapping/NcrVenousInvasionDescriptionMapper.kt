package com.hartwig.actin.personalization.ncr.interpretation.mapping

import com.hartwig.actin.personalization.datamodel.diagnosis.VenousInvasionDescription

object NcrVenousInvasionDescriptionMapper:  NcrIntCodeMapper<VenousInvasionDescription?> {

    override fun resolve(code: Int): VenousInvasionDescription? {
        return when (code) {
            0 -> VenousInvasionDescription.NONE
            1 -> VenousInvasionDescription.EXTRAMURAL
            2 -> VenousInvasionDescription.INTRAMURAL
            5 -> VenousInvasionDescription.SUSPECT
            8 -> VenousInvasionDescription.NA
            9 -> null
            else -> throw IllegalArgumentException("Unexpected value for VenousInvasionDescription: $code")
        }
    }
}