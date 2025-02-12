package com.hartwig.actin.personalization.datamodel

import com.hartwig.actin.personalization.datamodel.v2.outcome.ProgressionMeasureFollowUpEvent
import com.hartwig.actin.personalization.datamodel.v2.outcome.ProgressionMeasureType
import kotlinx.serialization.Serializable

@Serializable
data class PfsMeasure(
    val type: ProgressionMeasureType,
    val followUpEvent: ProgressionMeasureFollowUpEvent?,
    val intervalTumorIncidencePfsMeasureDays: Int?
)
