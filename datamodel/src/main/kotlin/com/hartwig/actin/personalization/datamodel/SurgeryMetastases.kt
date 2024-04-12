package com.hartwig.actin.personalization.datamodel

data class SurgeryMetastases(
    val surgeryMetastasesType: SurgeryMetastasesType,
    val surgeryMetastasesRadicality: SurgeryRadicality?,
    val intervalTumorIncidenceSurgeryMetastases: Int?,
    val intervalTumorIncidenceRMetastasesStop: Int?,
)
