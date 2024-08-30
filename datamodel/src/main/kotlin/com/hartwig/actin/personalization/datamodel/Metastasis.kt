package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class Metastasis (
    val location: Location,
    val intervalTumorIncidenceMetastasisDetectionDays: Int?,
    val isPartOfProgression: Boolean?
)