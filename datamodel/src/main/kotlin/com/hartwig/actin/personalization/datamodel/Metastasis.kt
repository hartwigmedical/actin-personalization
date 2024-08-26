package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class Metastasis (
    val location: Location,
    val intervalTumorIncidenceMetastasisDetection: Int?,
    val isPartOfProgression: Boolean?
)