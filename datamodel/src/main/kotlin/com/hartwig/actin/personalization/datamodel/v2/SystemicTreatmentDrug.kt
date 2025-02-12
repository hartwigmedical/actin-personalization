package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.Drug
import com.hartwig.actin.personalization.datamodel.TreatmentIntent
import kotlinx.serialization.Serializable

@Serializable
data class SystemicTreatmentDrug(
    val daysBetweenDiagnosisAndStart: Int?,
    val daysBetweenDiagnosisAndStop: Int?,
    val drug: Drug,
    val numberOfCycles: Int?,
    val intent: TreatmentIntent?,
    val drugTreatmentIsOngoing: Boolean?,
    val isAdministeredPreSurgery: Boolean?,
    val isAdministeredPostSurgery: Boolean?
)
