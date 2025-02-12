package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.Location
import kotlinx.serialization.Serializable

@Serializable
data class Metastasis(
    val daysSinceDiagnosis: Int?,
    val location: Location,
    val isLinkedToProgression: Boolean?
)
