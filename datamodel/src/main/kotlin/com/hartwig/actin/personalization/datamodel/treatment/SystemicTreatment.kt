package com.hartwig.actin.personalization.datamodel.treatment

import kotlinx.serialization.Serializable

@Serializable
data class SystemicTreatment(
    // TODO (KD) All day intervals and Treatment can be derived from components
    val daysBetweenDiagnosisAndStart: Int? = null,
    val daysBetweenDiagnosisAndStop: Int? = null,
    val treatment: Treatment,
    val schemes: List<SystemicTreatmentScheme>,
)
