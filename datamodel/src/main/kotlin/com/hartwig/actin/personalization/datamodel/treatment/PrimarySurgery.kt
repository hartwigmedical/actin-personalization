package com.hartwig.actin.personalization.datamodel.treatment

import kotlinx.serialization.Serializable

@Serializable
data class PrimarySurgery(
    val daysSinceDiagnosis: Int? = null,
    val type: SurgeryType,
    val technique: SurgeryTechnique? = null,
    val urgency: SurgeryUrgency? = null,
    val radicality: SurgeryRadicality? = null,
    val circumferentialResectionMargin: CircumferentialResectionMargin? = null,
    val anastomoticLeakageAfterSurgery: AnastomoticLeakageAfterSurgery? = null,
    val hospitalizationDurationDays: Int? = null
)
