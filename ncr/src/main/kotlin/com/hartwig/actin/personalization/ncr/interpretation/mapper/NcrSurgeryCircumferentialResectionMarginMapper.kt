package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.treatment.SurgeryCircumferentialResectionMargin

object NcrSurgeryCircumferentialResectionMarginMapper : NcrIntCodeMapper<SurgeryCircumferentialResectionMargin?> {

    override fun resolve(code: Int): SurgeryCircumferentialResectionMargin? {
        return when (code) {
            0 -> SurgeryCircumferentialResectionMargin.RESECTION_MARGINS_NOT_FREE
            1 -> SurgeryCircumferentialResectionMargin.RESECTION_MARGIN_BETWEEN_ZERO_AND_ONE_MM
            2 -> SurgeryCircumferentialResectionMargin.RESECTION_MARGIN_AT_LEAST_ONE_MM
            3 -> SurgeryCircumferentialResectionMargin.WIDE_RESECTION_MARGIN
            4 -> SurgeryCircumferentialResectionMargin.TIGHT_RADICAL
            5 -> SurgeryCircumferentialResectionMargin.AT_STAGE_PT0
            8 -> SurgeryCircumferentialResectionMargin.NA
            9 -> null
            else -> throw IllegalArgumentException("Unknown SurgeryCircumferentialResectionMargin code: $code")
        }
    }
}
