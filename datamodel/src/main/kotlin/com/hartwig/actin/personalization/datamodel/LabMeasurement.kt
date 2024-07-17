package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class LabMeasurement(
    val labMeasure: LabMeasure,
    val labMeasureValue: Double,
    val labMeasureUnit: LabMeasureUnit,
    val intervalTumorIncidenceLabMeasureValue: Int?,
    val isPreSurgical: Boolean?,
    val isPostSurgical: Boolean?,
)