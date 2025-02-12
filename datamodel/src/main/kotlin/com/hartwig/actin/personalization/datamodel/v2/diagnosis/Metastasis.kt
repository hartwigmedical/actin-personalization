package com.hartwig.actin.personalization.datamodel.v2.diagnosis

import kotlinx.serialization.Serializable

@Serializable
data class Metastasis(
    val daysSinceDiagnosis: Int?,
    val location: Location,
    val isLinkedToProgression: Boolean?
)
