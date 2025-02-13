package com.hartwig.actin.personalization.datamodel.old

import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocation
import kotlinx.serialization.Serializable

@Serializable
data class Metastasis(
    val location: TumorLocation,
    val intervalTumorIncidenceMetastasisDetectionDays: Int?,
    val isLinkedToProgression: Boolean?
)