package com.hartwig.actin.personalization.datamodel

data class PfsMeasure(
    val pfsMeasureType: PfsMeasureType,
    val pfsMeasureFollowupEvent: PfsMeasureFollowUpEvent?,
    val intervalTumorIncidencePfsMeasureDate: Int?
)
