package com.hartwig.actin.personalization.datamodel.assessment

import kotlinx.serialization.Serializable

@Serializable
data class WhoAssessment(
    val daysSinceDiagnosis: Int,
    val whoStatus: Int
)
