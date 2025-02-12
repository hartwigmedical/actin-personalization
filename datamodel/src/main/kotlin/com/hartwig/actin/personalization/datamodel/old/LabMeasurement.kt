package com.hartwig.actin.personalization.datamodel.old

import com.hartwig.actin.personalization.datamodel.v2.assessment.LabMeasure
import com.hartwig.actin.personalization.datamodel.v2.assessment.Unit
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