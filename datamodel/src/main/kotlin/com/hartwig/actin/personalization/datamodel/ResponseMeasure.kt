package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class ResponseMeasure(
    val measure: ResponseMeasureCategory,
    val intervalTumorIncidenceResponseMeasureDays: Int?
)
