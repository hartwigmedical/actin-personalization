package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.ResponseType

data class ResponseMeasure(
    val daysSinceDiagnosis: Int?,
    val response: ResponseType
)
