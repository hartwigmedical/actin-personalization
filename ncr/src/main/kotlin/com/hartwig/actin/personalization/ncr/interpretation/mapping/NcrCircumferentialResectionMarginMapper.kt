package com.hartwig.actin.personalization.ncr.interpretation.mapping

import com.hartwig.actin.personalization.datamodel.treatment.CircumferentialResectionMargin

object NcrCircumferentialResectionMarginMapper : NcrIntCodeMapper<CircumferentialResectionMargin?> {

    override fun resolve(code: Int): CircumferentialResectionMargin? {
        return when (code) {
            0 -> CircumferentialResectionMargin.RESECTION_MARGINS_NOT_FREE
            1 -> CircumferentialResectionMargin.RESECTION_MARGIN_BETWEEN_ZERO_AND_ONE_MM
            2 -> CircumferentialResectionMargin.RESECTION_MARGIN_AT_LEAST_ONE_MM
            3 -> CircumferentialResectionMargin.WIDE_RESECTION_MARGIN
            4 -> CircumferentialResectionMargin.TIGHT_RADICAL
            5 -> CircumferentialResectionMargin.AT_STAGE_PT0
            8 -> CircumferentialResectionMargin.NA
            9 -> null
            else -> throw IllegalArgumentException("Unknown SurgeryCircumferentialResectionMargin code: $code")
        }
    }
}
