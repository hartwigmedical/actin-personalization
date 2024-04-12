package com.hartwig.actin.personalization.datamodel

data class Surgery(
    val surgeryType: SurgeryType,
    val surgeryTechnique: SurgeryTechnique?,
    val surgeryUrgency: SurgeryUrgency?,
    val surgeryRadicality: SurgeryRadicality?,
    val circumferentialResectionMargin: SurgeryCircumferentialResectionMargin?,
    val anastomoticLeakageAfterSurgery: AnastomoticLeakageAfterSurgery?,
    val intervalTumorIncidenceSurgery: Int?,
    val durationOfHospitalization: Int?,

    )
