package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.TnmN

object NcrTnmNMapper : NcrStringCodeMapper<TnmN?> {
    override fun resolve(code: String): TnmN {
        return when (code) {
            "0" -> TnmN.N0
            "1" -> TnmN.N1
            "1A", "1Ami" -> TnmN.N1A
            "1B" -> TnmN.N1B
            "1C" -> TnmN.N1C
            "1M" -> TnmN.N1M
            "2" -> TnmN.N2
            "2A" -> TnmN.N2A
            "2B" -> TnmN.N2B
            "X" -> TnmN.X
            else -> throw IllegalArgumentException("Unknown TnmN code: $code")
        }
    }
}
