package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.PfsMeasureFollowUpEvent
import com.hartwig.actin.personalization.datamodel.PfsMeasureType
import kotlinx.serialization.Serializable

@Serializable
data class ProgressionMeasure (
    val daysSinceDiagnosis: Int?,
    val type: PfsMeasureType,
    val followUpEvent: PfsMeasureFollowUpEvent?
)