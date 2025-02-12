package com.hartwig.actin.personalization.datamodel.old

import com.hartwig.actin.personalization.datamodel.v2.outcome.ResponseType
import kotlinx.serialization.Serializable

@Serializable
data class ResponseMeasure(
    val responseType: ResponseType,
    val intervalTumorIncidenceResponseDays: Int?
)