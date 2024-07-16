package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class SystemicTreatmentComponent(
    val drug: Drug,
    val treatmentSchemeNumber: Int?, //Nullable because absent for prior tumors
    val treatmentNumberOfCycles: Int?,
    val treatmentCyclesDetails: TreatmentCyclesDetails?,
    val intervalTumorIncidenceTreatmentStart: Int?,
    val intervalTumorIncidenceTreatmentStop: Int?,
    val preSurgery: Boolean?,
    val postSurgery: Boolean?
)
