package com.hartwig.actin.personalization.datamodel

data class ResponseMeasure(
    val responseMeasureType: ResponseMeasureType,
    val intervalTreatmentPlanStartResponseDate: Int? //Calculate as (intervalTumorIncidenceResponseMeasureDate-intervalTumorIncidenceTreatmentPlanStart)
)
