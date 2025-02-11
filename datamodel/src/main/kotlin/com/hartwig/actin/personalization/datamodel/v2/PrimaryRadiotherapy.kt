package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.RadiotherapyType

data class PrimaryRadiotherapy(
    val daysBetweenDiagnosisAndStart: Int?,
    val daysBetweenDiagnosisAndStop: Int?,
    val type: RadiotherapyType,
    val totalDosage: Double?,
)
