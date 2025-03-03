package com.hartwig.actin.personalization.datamodel.old

import com.hartwig.actin.personalization.datamodel.treatment.AnastomoticLeakageAfterSurgery
import com.hartwig.actin.personalization.datamodel.treatment.CircumferentialResectionMargin
import com.hartwig.actin.personalization.datamodel.treatment.SurgeryRadicality
import com.hartwig.actin.personalization.datamodel.treatment.SurgeryTechnique
import com.hartwig.actin.personalization.datamodel.treatment.SurgeryType
import com.hartwig.actin.personalization.datamodel.treatment.SurgeryUrgency
import kotlinx.serialization.Serializable

@Serializable
data class Surgery(
    val type: SurgeryType,
    val technique: SurgeryTechnique? = null,
    val urgency: SurgeryUrgency? = null,
    val radicality: SurgeryRadicality? = null,
    val circumferentialResectionMargin: CircumferentialResectionMargin? = null,
    val anastomoticLeakageAfterSurgery: AnastomoticLeakageAfterSurgery? = null,
    val intervalTumorIncidenceSurgeryDays: Int? = null,
    val hospitalizationDurationDays: Int? = null
)