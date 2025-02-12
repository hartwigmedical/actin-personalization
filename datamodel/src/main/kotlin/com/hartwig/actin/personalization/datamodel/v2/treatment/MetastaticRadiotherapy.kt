package com.hartwig.actin.personalization.datamodel.v2.treatment

import kotlinx.serialization.Serializable

@Serializable
data class MetastaticRadiotherapy(
    val daysBetweenDiagnosisAndStart: Int?,
    val daysBetweenDiagnosisAndStop: Int?,
    val type: MetastasesRadiotherapyType,
)
