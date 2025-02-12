package com.hartwig.actin.personalization.datamodel

import com.hartwig.actin.personalization.datamodel.v2.diagnosis.Location
import kotlinx.serialization.Serializable

@Serializable
data class Metastasis (
    val location: Location,
    val intervalTumorIncidenceMetastasisDetectionDays: Int?,
    val isLinkedToProgression: Boolean?
)