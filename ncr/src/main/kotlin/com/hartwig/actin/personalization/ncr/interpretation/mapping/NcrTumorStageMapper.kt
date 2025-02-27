package com.hartwig.actin.personalization.ncr.interpretation.mapping

import com.hartwig.actin.personalization.datamodel.diagnosis.TumorStage

object NcrTumorStageMapper : NcrStringCodeMapper<TumorStage> {
    override fun resolve(code: String): TumorStage {
        return when (code) {
            "0" -> TumorStage.ZERO
            "1" -> TumorStage.I
            "1A" -> TumorStage.IA
            "1A1" -> TumorStage.IA1
            "1A2" -> TumorStage.IA2
            "1A3" -> TumorStage.IA3
            "1B" -> TumorStage.IB
            "2" -> TumorStage.II
            "2A" -> TumorStage.IIA
            "2B" -> TumorStage.IIB
            "2C" -> TumorStage.IIC
            "3" -> TumorStage.III
            "3A" -> TumorStage.IIIA
            "3B" -> TumorStage.IIIB
            "3C" -> TumorStage.IIIC
            "4" -> TumorStage.IV
            "4A" -> TumorStage.IVA
            "4B" -> TumorStage.IVB
            "4C" -> TumorStage.IVC
            "M" -> TumorStage.M
            "NVT" -> TumorStage.NA
            "X" -> TumorStage.X
            else -> throw IllegalArgumentException("Unknown TumorStage code: $code")
        }
    }
}
