package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.ReasonRefrainmentFromTumorDirectedTreatment

object NcrReasonRefrainmentFromTumorDirectedTherapyMapper : NcrIntCodeMapper<ReasonRefrainmentFromTumorDirectedTreatment?> {

    override fun resolve(code: Int): ReasonRefrainmentFromTumorDirectedTreatment? {
        return when (code) {
            8, 9 -> null
            11 -> ReasonRefrainmentFromTumorDirectedTreatment.COMORBIDITY_AND_OR_PERFORMANCE_OR_FUNCTIONAL_STATUS_OR_PRESENCE_OTHER_TUMOR
            12 -> ReasonRefrainmentFromTumorDirectedTreatment.EXPECTED_FAST_PROGRESSION_OR_HIGH_TUMOR_LOAD
            13 -> ReasonRefrainmentFromTumorDirectedTreatment.WISH_OR_REFUSAL_FROM_PATIENT_OR_FAMILY
            14 -> ReasonRefrainmentFromTumorDirectedTreatment.LIMITED_TUMOR_LOAD_OR_FEW_COMPLAINTS
            else -> throw IllegalArgumentException("Unknown ReasonRefrainmentFromTumorDirectedTreatment code: $code")
        }
    }
}