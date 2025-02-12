package com.hartwig.actin.personalization.datamodel.old

import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureFollowUpEvent
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureType
import kotlinx.serialization.Serializable

@Serializable
data class PfsMeasure(
    val type: ProgressionMeasureType,
    val followUpEvent: ProgressionMeasureFollowUpEvent?,
    val intervalTumorIncidencePfsMeasureDays: Int?
)