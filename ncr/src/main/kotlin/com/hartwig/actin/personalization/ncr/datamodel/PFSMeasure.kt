package com.hartwig.actin.personalization.ncr.datamodel

data class PFSMeasure(
    val pfsMeasureType: PFSMeasureType,
    val pfsMeasureFollowupEvent: PFSMeasureFollowUpEvent?,
    val intervalTumorIncidencePFSMeasureDate: Int?
)
