package com.hartwig.actin.personalization.datamodel.v2.outcome

import kotlinx.serialization.Serializable

@Serializable
data class ProgressionMeasure (
    val daysSinceDiagnosis: Int?,
    val type: ProgressionMeasureType,
    val followUpEvent: ProgressionMeasureFollowUpEvent?
)