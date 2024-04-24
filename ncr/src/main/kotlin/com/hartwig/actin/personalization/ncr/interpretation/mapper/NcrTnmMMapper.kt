package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.TnmM
import com.hartwig.actin.personalization.ncr.interpretation.NcrStringCodeMapper

object NcrTnmMMapper : NcrStringCodeMapper<TnmM?> {
    override fun resolve(code: String): TnmM? {
        return when (code) {
            "0" -> TnmM.M0
            "1" -> TnmM.M1
            "1A" -> TnmM.M1A
            "1B" -> TnmM.M1B
            "1C" -> TnmM.M1C
            "X" -> TnmM.X
            "" -> null
            else -> throw IllegalArgumentException("Unknown TnmM code: $code")
        }
    }
}