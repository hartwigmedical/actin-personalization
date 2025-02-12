package com.hartwig.actin.personalization.datamodel.outcome

import kotlinx.serialization.Serializable

@Serializable
data class ProgressionMeasure (
    val daysSinceDiagnosis: Int?,
    val type: ProgressionMeasureType,
    val followUpEvent: ProgressionMeasureFollowUpEvent?
)