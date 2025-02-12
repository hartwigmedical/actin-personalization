package com.hartwig.actin.personalization.datamodel.v2

import kotlinx.serialization.Serializable

@Serializable
data class LatestSurvivalStatus(
    val daysSinceDiagnosis: Int,
    val isAlive: Boolean
)
