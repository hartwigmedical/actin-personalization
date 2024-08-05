package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class PfsMeasure(
    val type: PfsMeasureType,
    val followUpEvent: PfsMeasureFollowUpEvent?,
    val intervalTumorIncidencePfsMeasure: Int?
)
