package com.hartwig.actin.personalization.datamodel.v2.treatment

import kotlinx.serialization.Serializable

@Serializable
data class SystemicTreatment(
    val daysBetweenDiagnosisAndStart: Int? = null,
    val daysBetweenDiagnosisAndStop: Int? = null,
    val treatment: Treatment,
    val systemicTreatmentSchemes: List<SystemicTreatmentScheme>,
)
