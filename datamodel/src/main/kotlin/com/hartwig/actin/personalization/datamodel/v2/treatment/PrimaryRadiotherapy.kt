package com.hartwig.actin.personalization.datamodel.v2.treatment

import kotlinx.serialization.Serializable

@Serializable
data class PrimaryRadiotherapy(
    val daysBetweenDiagnosisAndStart: Int?,
    val daysBetweenDiagnosisAndStop: Int?,
    val type: RadiotherapyType,
    val totalDosage: Double?,
)
