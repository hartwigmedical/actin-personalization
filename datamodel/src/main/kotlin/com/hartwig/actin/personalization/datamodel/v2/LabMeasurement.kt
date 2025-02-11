package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.LabMeasure

data class LabMeasurement(
    val daysSinceDiagnosis: Int?,

    val name: LabMeasure,
    val value: Double,
    val unit: Unit,
    val isPreSurgical: Boolean?,
    val isPostSurgical: Boolean?
)
