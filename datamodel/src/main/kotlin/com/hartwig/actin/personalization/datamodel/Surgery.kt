package com.hartwig.actin.personalization.datamodel

data class Surgery(
    val surgeryType: SurgeryType,
    val surgeryTechnique: SurgeryTechnique? = null,
    val surgeryUrgency: SurgeryUrgency? = null,
    val surgeryRadicality: SurgeryRadicality? = null,
    val circumferentialResectionMargin: SurgeryCircumferentialResectionMargin? = null,
    val anastomoticLeakageAfterSurgery: AnastomoticLeakageAfterSurgery? = null,
    val intervalTumorIncidenceSurgery: Int? = null,
    val durationOfHospitalization: Int? = null
)
