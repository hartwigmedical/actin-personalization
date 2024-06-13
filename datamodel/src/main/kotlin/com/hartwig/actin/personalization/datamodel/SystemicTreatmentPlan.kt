package com.hartwig.actin.personalization.datamodel

data class SystemicTreatmentPlan( //Should be based on combination of all treatment schemes of same episode
    val treatmentPlan: SystemicTreatmentPlanName,
    val intervalTumorIncidenceTreatmentPlanStart: Int?, //intervalTumorIncidenceTreatmentLineStartMin of scheme nr = 1
    val intervalTumorIncidenceTreatmentPlanStop: Int?, //intervalTumorIncidenceTreatmentLineStopMax of last scheme nr

    val treatmentPlanResponses: List<ResponseMeasure>, //All responses of the episode
    val treatmentPlanBestResponse: ResponseMeasure?, //CR>PR>MR>SD>PD
    val treatmentPlanPfsValues: List<PfsMeasure>, //All pfs measure entries
    val treatmentPlanPfs: PfsMeasureType?, // Calculate as minimal (intervalTumorIncidencePfsMeasureDate-intervalTumorIncidenceTreatmentPlanStart) from all available PFS measures of Type PROGRESSION or DEATH (so ignore CENSOR for now), and any FollowUpEvent.
)
