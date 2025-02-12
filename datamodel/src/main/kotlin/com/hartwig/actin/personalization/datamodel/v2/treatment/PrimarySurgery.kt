package com.hartwig.actin.personalization.datamodel.v2.treatment

import kotlinx.serialization.Serializable

@Serializable
data class PrimarySurgery(
    val daysSinceDiagnosis: Int? = null,
    val type: SurgeryType,
    val technique: SurgeryTechnique? = null,
    val urgency: SurgeryUrgency? = null,
    val radicality: SurgeryRadicality? = null,
    val circumferentialResectionMargin: SurgeryCircumferentialResectionMargin? = null,
    val anastomoticLeakageAfterSurgery: AnastomoticLeakageAfterSurgery? = null,
    val hospitalizationDurationDays: Int? = null
)
