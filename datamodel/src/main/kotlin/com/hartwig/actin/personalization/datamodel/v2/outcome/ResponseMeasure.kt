package com.hartwig.actin.personalization.datamodel.v2.outcome

import kotlinx.serialization.Serializable

@Serializable
data class ResponseMeasure(
    val daysSinceDiagnosis: Int?,
    val response: ResponseType
)
