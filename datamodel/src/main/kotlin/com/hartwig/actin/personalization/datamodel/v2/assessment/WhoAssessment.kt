package com.hartwig.actin.personalization.datamodel.v2.assessment

import kotlinx.serialization.Serializable

@Serializable
data class WhoAssessment(
    val daysSinceDiagnosis: Int,
    val whoStatus: Int
)
