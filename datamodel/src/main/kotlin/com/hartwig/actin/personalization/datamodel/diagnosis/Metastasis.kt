package com.hartwig.actin.personalization.datamodel.diagnosis

import kotlinx.serialization.Serializable

@Serializable
data class Metastasis(
    val daysSinceDiagnosis: Int?,
    val location: TumorLocation,
    val isLinkedToProgression: Boolean?
)
