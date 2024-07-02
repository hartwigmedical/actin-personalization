package com.hartwig.actin.personalization.datamodel

data class SystemicTreatmentPlan(
    val treatment: Treatment,
    val systemicTreatmentSchemes: List<SystemicTreatmentScheme>,
    val intervalTumorIncidenceTreatmentPlanStart: Int? = null,
    val intervalTumorIncidenceTreatmentPlanStop: Int? = null,
    val intervalTreatmentPlanStartLatestAliveStatus: Int? = null,

    val pfs: Int? = null,
    val intervalTreatmentPlanStartResponseDate: Int? = null
)
