package com.hartwig.actin.personalization.ncr.datamodel

data class SystemicTreatment(
    val treatmentName: TreatmentName,
    val treatmentSchemeNumber: Int,
    val treatmentNumberOfCycles: Int?,
    val treatmentCyclesDetails: TreatmentCyclesDetails?,
    val intervalTumorIncidenceTreatmentStart: Int,
    val intervalTumorIncidenceTreatmentStop: Int,
)
