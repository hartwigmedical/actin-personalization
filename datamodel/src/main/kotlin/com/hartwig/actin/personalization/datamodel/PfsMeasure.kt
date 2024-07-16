package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class PfsMeasure(
    val pfsMeasureType: PfsMeasureType,
    val pfsMeasureFollowupEvent: PfsMeasureFollowUpEvent?,
    val intervalTumorIncidencePfsMeasureDate: Int?
)
