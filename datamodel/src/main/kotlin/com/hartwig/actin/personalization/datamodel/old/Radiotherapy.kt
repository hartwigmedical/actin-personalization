package com.hartwig.actin.personalization.datamodel.old

import com.hartwig.actin.personalization.datamodel.v2.treatment.RadiotherapyType
import kotlinx.serialization.Serializable

@Serializable
data class Radiotherapy(
    val radiotherapyType: RadiotherapyType,
    val radiotherapyTotalDosage: Double?,
    val intervalTumorIncidenceRadiotherapyStartDays: Int?,
    val intervalTumorIncidenceRadiotherapyStopDays: Int?,
)