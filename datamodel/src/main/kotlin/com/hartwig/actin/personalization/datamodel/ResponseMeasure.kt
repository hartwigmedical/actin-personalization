package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class ResponseMeasure(
    val responseMeasureType: ResponseMeasureType,
    val intervalTumorIncidenceResponseMeasureDate: Int?
)
