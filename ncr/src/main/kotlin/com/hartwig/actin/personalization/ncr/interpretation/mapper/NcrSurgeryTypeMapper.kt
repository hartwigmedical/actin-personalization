package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.treatment.SurgeryType

object NcrSurgeryTypeMapper : NcrIntCodeMapper<SurgeryType> {

    override fun resolve(code: Int): SurgeryType {
        return when (code) {
            1 -> SurgeryType.APPENDECTOMY
            2 -> SurgeryType.HEMICOLECTOMY_OR_ILEOCECAL_RESECTION
            3 -> SurgeryType.TRANSVERSUM_RESECTION
            4 -> SurgeryType.SIGMOID_RESECTION
            5 -> SurgeryType.SUBTOTAL_COLECTOMY
            6 -> SurgeryType.TRANSANAL_ENDOSCOPIC_MICROSURGERY
            7 -> SurgeryType.LOW_ANTERIOR_RESECTION
            8 -> SurgeryType.ABDOMINOPERINEAL_RESECTION
            9 -> SurgeryType.TOTAL_COLECTOMY
            10 -> SurgeryType.NOS_OR_OTHER
            else -> throw IllegalArgumentException("Unknown SurgeryType code: $code")
        }
    }
}
