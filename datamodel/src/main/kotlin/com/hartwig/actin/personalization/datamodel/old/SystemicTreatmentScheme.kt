package com.hartwig.actin.personalization.datamodel.old

import kotlinx.serialization.Serializable

@Serializable
data class SystemicTreatmentScheme(
    val schemeNumber: Int?,
    val treatmentComponents: List<SystemicTreatmentSchemeDrug>,
    val intervalTumorIncidenceTreatmentLineStartMinDays: Int?,
    val intervalTumorIncidenceTreatmentLineStartMaxDays: Int?,
    val intervalTumorIncidenceTreatmentLineStopMinDays: Int?,
    val intervalTumorIncidenceTreatmentLineStopMaxDays: Int?
)