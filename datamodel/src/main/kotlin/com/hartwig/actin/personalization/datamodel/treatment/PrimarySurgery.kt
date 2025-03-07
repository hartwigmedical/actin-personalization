package com.hartwig.actin.personalization.datamodel.treatment

import kotlinx.serialization.Serializable

@Serializable
data class PrimarySurgery(
    val daysSinceDiagnosis: Int?,
    val type: SurgeryType,
    val technique: SurgeryTechnique?,
    val urgency: SurgeryUrgency?,
    val radicality: SurgeryRadicality?,
    val circumferentialResectionMargin: CircumferentialResectionMargin?,
    val anastomoticLeakageAfterSurgery: AnastomoticLeakageAfterSurgery?,
    val hospitalizationDurationDays: Int?
)
