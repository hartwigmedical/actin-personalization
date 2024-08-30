package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class LabMeasurement(
    val name: LabMeasure,
    val value: Double,
    val unit: Unit,
    val intervalTumorIncidenceLabMeasureDays: Int?,
    val isPreSurgical: Boolean?,
    val isPostSurgical: Boolean?,
)