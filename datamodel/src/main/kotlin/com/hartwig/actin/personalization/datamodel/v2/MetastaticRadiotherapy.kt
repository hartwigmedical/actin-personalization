package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.MetastasesRadiotherapyType
import kotlinx.serialization.Serializable

@Serializable
data class MetastaticRadiotherapy(
    val daysBetweenDiagnosisAndStart: Int?,
    val daysBetweenDiagnosisAndStop: Int?,
    val type: MetastasesRadiotherapyType,
)
