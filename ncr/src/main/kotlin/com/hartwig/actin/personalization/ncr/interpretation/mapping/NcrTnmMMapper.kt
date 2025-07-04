package com.hartwig.actin.personalization.ncr.interpretation.mapping

import com.hartwig.actin.personalization.datamodel.diagnosis.TnmM

object NcrTnmMMapper : NcrStringCodeMapper<TnmM?> {
    override fun resolve(code: String): TnmM {
        return when (code) {
            "0" -> TnmM.M0
            "1" -> TnmM.M1
            "1A" -> TnmM.M1A
            "1B" -> TnmM.M1B
            "1C" -> TnmM.M1C
            "-" -> TnmM.M_MINUS
            "X" -> TnmM.X
            else -> throw IllegalArgumentException("Unknown TnmM code: $code")
        }
    }
}