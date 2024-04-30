package com.hartwig.actin.personalization.datamodel

data class LabMeasurement(
    val labMeasure: LabMeasure,
    val labMeasureValue: Double,
    val labMeasureUnit: LabMeasureUnit,
    val intervalTumorIncidenceLabMeasureValue: Int?,
    val isPreSurgical: Boolean?,
    val isPostSurgical: Boolean?,
)