package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.TnmT

object NcrTnmTMapper : NcrStringCodeMapper<TnmT?> {
    override fun resolve(code: String): TnmT {
        return when (code) {
            "0" -> TnmT.T0
            "IS" -> TnmT.T_IS
            "1" -> TnmT.T1
            "2" -> TnmT.T2
            "3" -> TnmT.T3
            "4A" -> TnmT.T4A
            "4B" -> TnmT.T4B
            "X" -> TnmT.X
            else -> throw IllegalArgumentException("Unknown TnmT code: $code")
        }
    }
}