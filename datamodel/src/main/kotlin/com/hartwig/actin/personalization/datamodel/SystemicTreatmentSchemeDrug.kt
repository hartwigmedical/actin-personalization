package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class SystemicTreatmentSchemeDrug(
    val drug: Drug,
    val schemeNumber: Int?, //Nullable because absent for prior tumors
    val numberOfCycles: Int?,
    val intent: TreatmentIntent?,
    val drugTreatmentIsOngoing: Boolean?,
    val intervalTumorIncidenceTreatmentStartDays: Int?,
    val intervalTumorIncidenceTreatmentStopDays: Int?,
    val isAdministeredPreSurgery: Boolean?,
    val isAdministeredPostSurgery: Boolean?
)
