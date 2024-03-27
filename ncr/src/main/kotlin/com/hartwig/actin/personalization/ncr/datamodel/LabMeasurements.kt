package com.hartwig.actin.personalization.ncr.datamodel

data class LabMeasurements(
    val labMeasure: LabMeasure,
    val labMeasureValue: Int?,
    val labMeasureUnit: LabMeasureUnit,
    val intervalTumorIncidenceLabMeasureValue: Int?,
    val isPrechirurgic: Boolean?,
    val isPostChirurgic: Boolean?,
)