package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.StageTnm
import com.hartwig.actin.personalization.ncr.interpretation.NcrStringCodeMapper

object NcrStageTnmMapper : NcrStringCodeMapper<StageTnm?> {
    override fun resolve(code: String): StageTnm? {
        return when (code) {
            "0" -> StageTnm.ZERO
            "1" -> StageTnm.I
            "2A" -> StageTnm.IIA
            "2B" -> StageTnm.IIB
            "2C" -> StageTnm.IIC
            "3A" -> StageTnm.IIIA
            "3B" -> StageTnm.IIIB
            "3C" -> StageTnm.IIIC
            "4A" -> StageTnm.IVA
            "4B" -> StageTnm.IVB
            "4C" -> StageTnm.IVC
            "NA" -> StageTnm.NA
            "X" -> StageTnm.X
            "NVT" -> null
            else -> throw IllegalArgumentException("Unknown StageTnm code: $code")
        }
    }
}