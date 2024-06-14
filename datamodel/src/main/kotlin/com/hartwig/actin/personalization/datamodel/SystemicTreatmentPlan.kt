package com.hartwig.actin.personalization.datamodel

data class SystemicTreatmentPlan(
    val treatment: Treatment,
    val systemicTreatmentSchemes: List<SystemicTreatmentScheme>,
    val intervalTumorIncidenceTreatmentPlanStart: Int?,
    val intervalTumorIncidenceTreatmentPlanStop: Int?,

    val pfs: Int?,
    val intervalTreatmentPlanStartResponseDate: Int?
)
