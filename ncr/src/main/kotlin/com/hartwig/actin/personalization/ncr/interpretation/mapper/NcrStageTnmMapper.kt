package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.StageTnm

object NcrStageTnmMapper : NcrStringCodeMapper<StageTnm> {
    override fun resolve(code: String): StageTnm {
        return when (code) {
            "0" -> StageTnm.ZERO
            "1" -> StageTnm.I
            "1A" -> StageTnm.IA
            "1A1" -> StageTnm.IA1
            "1A2" -> StageTnm.IA2
            "1A3" -> StageTnm.IA3
            "1B" -> StageTnm.IB
            "2" -> StageTnm.II
            "2A" -> StageTnm.IIA
            "2B" -> StageTnm.IIB
            "2C" -> StageTnm.IIC
            "3" -> StageTnm.III
            "3A" -> StageTnm.IIIA
            "3B" -> StageTnm.IIIB
            "3C" -> StageTnm.IIIC
            "4" -> StageTnm.IV
            "4A" -> StageTnm.IVA
            "4B" -> StageTnm.IVB
            "4C" -> StageTnm.IVC
            "M" -> StageTnm.M
            "NVT" -> StageTnm.NA
            "X" -> StageTnm.X
            else -> throw IllegalArgumentException("Unknown StageTnm code: $code")
        }
    }
}
