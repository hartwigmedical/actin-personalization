package com.hartwig.actin.personalization.ncr.datamodel

data class SystemicTreatmentLine(
    val treatmentLineName: TreatmentLineName,
    val drugs: List<Drug>,
    val intervalTumorIncidenceTreatmentLineStartMin: Int,
    val intervalTumorIncidenceTreatmentLineStartMax: Int,
    val intervalTumorIncidenceTreatmentLineStopMin: Int,
    val intervalTumorIncidenceTreatmentLineStopMax: Int,
)
