package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.Drug
import com.hartwig.actin.personalization.datamodel.TreatmentIntent

data class SystemicTreatmentSchemeDrug(
    val daysBetweenDiagnosisAndStart: Int?,
    val daysBetweenDiagnosisAndStop: Int?,
    val drug: Drug,
    val schemeNumber: Int?, //Nullable because absent for prior tumors
    val numberOfCycles: Int?,
    val intent: TreatmentIntent?,
    val drugTreatmentIsOngoing: Boolean?,
    val isAdministeredPreSurgery: Boolean?,
    val isAdministeredPostSurgery: Boolean?
)
