package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class Surgery(
    val type: SurgeryType,
    val technique: SurgeryTechnique? = null,
    val urgency: SurgeryUrgency? = null,
    val radicality: SurgeryRadicality? = null,
    val circumferentialResectionMargin: SurgeryCircumferentialResectionMargin? = null,
    val anastomoticLeakageAfterSurgery: AnastomoticLeakageAfterSurgery? = null,
    val intervalTumorIncidenceSurgeryDays: Int? = null,
    val hospitalizationDurationDays: Int? = null
)
