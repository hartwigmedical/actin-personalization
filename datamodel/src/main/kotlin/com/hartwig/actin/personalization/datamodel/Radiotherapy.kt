package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class Radiotherapy(
    val radiotherapyType: RadiotherapyType,
    val radiotherapyTotalDosage: Double?,
    val intervalTumorIncidenceRadiotherapyStartDays: Int?,
    val intervalTumorIncidenceRadiotherapyStopDays: Int?,
)
