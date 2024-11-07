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
    val observedOsFromTreatmentStartDays: Int? = null
){
    companion object {
        val NONE = SystemicTreatmentPlan(
            treatment = Treatment.NONE,
            systemicTreatmentSchemes = emptyList(),
            intervalTumorIncidenceTreatmentPlanStartDays = null,
            intervalTumorIncidenceTreatmentPlanStopDays = null,
            intervalTreatmentPlanStartResponseDays = null,
            observedPfsDays = null,
            hadProgressionEvent = null,
            observedOsFromTreatmentStartDays = null
        )
}
}
