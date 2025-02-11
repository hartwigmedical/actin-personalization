package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.Treatment

data class SystemicTreatmentPlan(
    val daysBetweenDiagnosisAndStart: Int? = null,
    val daysBetweenDiagnosisAndStop: Int? = null,
    val treatment: Treatment,
    val systemicTreatmentSchemes: List<SystemicTreatmentScheme>,

    val daysBetweenStartAndResponse: Int? = null,
    val observedPfsDays: Int? = null,
    val hadProgressionEvent: Boolean? = null,
    val observedOsFromTreatmentStartDays: Int? = null
)
