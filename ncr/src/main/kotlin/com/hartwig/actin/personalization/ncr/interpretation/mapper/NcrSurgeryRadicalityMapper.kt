package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.SurgeryRadicality

object NcrSurgeryRadicalityMapper : NcrIntCodeMapper<SurgeryRadicality?> {

    override fun resolve(code: Int): SurgeryRadicality? {
        return when (code) {
            0 -> SurgeryRadicality.MICROSCOPIC_RADICAL
            1 -> SurgeryRadicality.MICROSCOPIC_IRRADICAL
            2 -> SurgeryRadicality.MACROSCOPIC_IRRADICAL
            9 -> null
            else -> throw IllegalArgumentException("Unknown SurgeryRadicality code: $code")
        }
    }
}
