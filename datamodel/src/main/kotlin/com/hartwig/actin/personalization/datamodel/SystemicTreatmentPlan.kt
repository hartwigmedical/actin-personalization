package com.hartwig.actin.personalization.datamodel

data class SystemicTreatmentPlan( //Should be based on combination of all treatment schemes of same episode
    val treatmentPlan: Treatment,
    val intervalTumorIncidenceTreatmentPlanStart: Int?, //intervalTumorIncidenceTreatmentLineStartMin of scheme nr = 1
    val intervalTumorIncidenceTreatmentPlanStop: Int?, //intervalTumorIncidenceTreatmentLineStopMax of last scheme nr

    val pfs: Int?, // IntervalTreatmentPlanStartResponseDate from all available PFS measures of Type PROGRESSION or DEATH (so ignore CENSOR for now), and any FollowUpEvent.
)
