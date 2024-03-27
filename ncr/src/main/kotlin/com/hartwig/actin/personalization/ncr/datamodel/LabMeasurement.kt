package com.hartwig.actin.personalization.ncr.datamodel

data class LabMeasurement(
    val labMeasure: LabMeasure,
    val labMeasureValue: Int,
    val labMeasureUnit: LabMeasureUnit,
    val intervalTumorIncidenceLabMeasureValue: Int?,
    val isPreChirurgic: Boolean?,
    val isPostChirurgic: Boolean?,
)