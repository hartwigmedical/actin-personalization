package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class SystemicTreatmentScheme(
    //Should combine treatments of same scheme number
    val schemeNumber: Int?,
    val treatmentComponents: List<SystemicTreatmentSchemeDrug>,
    val intervalTumorIncidenceTreatmentLineStartMin: Int?,
    val intervalTumorIncidenceTreatmentLineStartMax: Int?,
    val intervalTumorIncidenceTreatmentLineStopMin: Int?,
    val intervalTumorIncidenceTreatmentLineStopMax: Int?
)