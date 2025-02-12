package com.hartwig.actin.personalization.datamodel.v2.treatment

import kotlinx.serialization.Serializable

@Serializable
data class SystemicTreatmentScheme (
    // TODO (KD) Can be derived from components?
    val minDaysBetweenDiagnosisAndStart: Int?,
    val maxDaysBetweenDiagnosisAndStart: Int?,
    val minDaysBetweenDiagnosisAndStop: Int?,
    val maxDaysBetweenDiagnosisAndStop: Int?,

    val components: List<SystemicTreatmentDrug>
)
