package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class ResponseMeasure(
    val responseType: ResponseType,
    val intervalTumorIncidenceResponseDays: Int?
)
