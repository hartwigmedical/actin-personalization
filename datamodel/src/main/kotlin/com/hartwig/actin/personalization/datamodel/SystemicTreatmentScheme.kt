package com.hartwig.actin.personalization.datamodel

data class SystemicTreatmentScheme(
    //Should combine treatments of same scheme number
    val schemeNumber: Int?,
    val treatmentComponents: List<SystemicTreatmentComponent>,
    val intervalTumorIncidenceTreatmentLineStartMin: Int?,
    val intervalTumorIncidenceTreatmentLineStartMax: Int?,
    val intervalTumorIncidenceTreatmentLineStopMin: Int?,
    val intervalTumorIncidenceTreatmentLineStopMax: Int?
)