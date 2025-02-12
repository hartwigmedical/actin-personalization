package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.AnastomoticLeakageAfterSurgery
import com.hartwig.actin.personalization.datamodel.SurgeryCircumferentialResectionMargin
import com.hartwig.actin.personalization.datamodel.SurgeryRadicality
import com.hartwig.actin.personalization.datamodel.SurgeryTechnique
import com.hartwig.actin.personalization.datamodel.SurgeryType
import com.hartwig.actin.personalization.datamodel.SurgeryUrgency
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
