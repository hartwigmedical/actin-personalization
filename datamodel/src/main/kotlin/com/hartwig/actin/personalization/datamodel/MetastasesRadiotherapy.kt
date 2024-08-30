package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class MetastasesRadiotherapy(
    val metastasesRadiotherapyType: MetastasesRadiotherapyType,
    val intervalTumorIncidenceRadiotherapyMetastasesStartDays: Int?,
    val intervalTumorIncidenceRadiotherapyMetastasesStopDays: Int?,
)