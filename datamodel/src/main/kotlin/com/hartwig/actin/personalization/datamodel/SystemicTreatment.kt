package com.hartwig.actin.personalization.datamodel

data class SystemicTreatment(
    val treatmentName: TreatmentName,
    val treatmentCategory: TreatmentCategory,
    val treatmentSchemeNumber: Int?, //Nullable because absent for prior tumors
    val treatmentNumberOfCycles: Int?,
    val treatmentCyclesDetails: TreatmentCyclesDetails?,
    val intervalTumorIncidenceTreatmentStart: Int,
    val intervalTumorIncidenceTreatmentStop: Int,
)
