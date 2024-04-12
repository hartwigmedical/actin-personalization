package com.hartwig.actin.personalization.datamodel

data class PFSMeasure(
    val pfsMeasureType: PFSMeasureType,
    val pfsMeasureFollowupEvent: PFSMeasureFollowUpEvent?,
    val intervalTumorIncidencePFSMeasureDate: Int?
)
