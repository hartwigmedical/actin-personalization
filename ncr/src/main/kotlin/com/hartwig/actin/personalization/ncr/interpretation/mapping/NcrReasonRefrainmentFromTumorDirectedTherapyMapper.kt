package com.hartwig.actin.personalization.ncr.interpretation.mapping

import com.hartwig.actin.personalization.datamodel.treatment.ReasonRefrainmentFromTreatment

object NcrReasonRefrainmentFromTumorDirectedTherapyMapper : NcrIntCodeMapper<ReasonRefrainmentFromTreatment?> {

    override fun resolve(code: Int): ReasonRefrainmentFromTreatment? {
        return when (code) {
            8, 9 -> null
            11 -> ReasonRefrainmentFromTreatment.COMORBIDITY_AND_OR_PERFORMANCE_OR_FUNCTIONAL_STATUS_OR_PRESENCE_OTHER_TUMOR
            12 -> ReasonRefrainmentFromTreatment.EXPECTED_FAST_PROGRESSION_OR_HIGH_TUMOR_LOAD
            13 -> ReasonRefrainmentFromTreatment.WISH_OR_REFUSAL_FROM_PATIENT_OR_FAMILY
            14 -> ReasonRefrainmentFromTreatment.LIMITED_TUMOR_LOAD_OR_FEW_COMPLAINTS
            else -> throw IllegalArgumentException("Unknown ReasonRefrainmentFromTumorDirectedTreatment code: $code")
        }
    }
}