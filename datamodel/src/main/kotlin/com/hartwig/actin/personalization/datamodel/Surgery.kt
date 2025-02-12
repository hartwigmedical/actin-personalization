package com.hartwig.actin.personalization.datamodel

import com.hartwig.actin.personalization.datamodel.v2.treatment.AnastomoticLeakageAfterSurgery
import com.hartwig.actin.personalization.datamodel.v2.treatment.SurgeryCircumferentialResectionMargin
import com.hartwig.actin.personalization.datamodel.v2.treatment.SurgeryRadicality
import com.hartwig.actin.personalization.datamodel.v2.treatment.SurgeryTechnique
import com.hartwig.actin.personalization.datamodel.v2.treatment.SurgeryType
import com.hartwig.actin.personalization.datamodel.v2.treatment.SurgeryUrgency
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
