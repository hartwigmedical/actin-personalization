package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class SystemicTreatmentPlan(
    val treatment: Treatment,
    val systemicTreatmentSchemes: List<SystemicTreatmentScheme>,
    val intervalTumorIncidenceTreatmentPlanStartDays: Int? = null,
    val intervalTumorIncidenceTreatmentPlanStopDays: Int? = null,

    val intervalTreatmentPlanStartResponseDays: Int? = null,
    val observedPfsDays: Int? = null,
    val hadProgressionEvent: Boolean? = null,

    val observedOverallSurvivalFromTreatmentPlanStart: Int? = null,
)
