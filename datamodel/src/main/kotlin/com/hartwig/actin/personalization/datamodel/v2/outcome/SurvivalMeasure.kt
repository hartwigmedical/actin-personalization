package com.hartwig.actin.personalization.datamodel.v2.outcome

import kotlinx.serialization.Serializable

@Serializable
data class SurvivalMeasure(
    val daysSinceDiagnosis: Int,
    val isAlive: Boolean
)
