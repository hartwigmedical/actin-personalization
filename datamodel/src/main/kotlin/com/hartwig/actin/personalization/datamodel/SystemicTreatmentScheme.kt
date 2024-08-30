package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class SystemicTreatmentScheme(
    //Should combine treatments of same scheme number
    val schemeNumber: Int?,
    val treatmentComponents: List<SystemicTreatmentSchemeDrug>,
    val intervalTumorIncidenceTreatmentLineStartMinDays: Int?,
    val intervalTumorIncidenceTreatmentLineStartMaxDays: Int?,
    val intervalTumorIncidenceTreatmentLineStopMinDays: Int?,
    val intervalTumorIncidenceTreatmentLineStopMaxDays: Int?
)