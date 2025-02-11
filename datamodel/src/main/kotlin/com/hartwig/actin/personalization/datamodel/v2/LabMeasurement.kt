package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.LabMeasure

data class LabMeasurement(
    val name: LabMeasure,
    val value: Double,
    val unit: Unit,
    val daysBetweenDiagnosisAndMeasurement: Int?,
    val isPreSurgical: Boolean?,
    val isPostSurgical: Boolean?
)
