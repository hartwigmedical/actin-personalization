package com.hartwig.actin.personalization.ncr.datamodel

data class SurgeryMetastases(
    val surgeryMetastasesType: SurgeryMetastasesType,
    val surgeryMetastasesRadicality: SurgeryRadicality?,
    val intervalTumorIncidenceSurgeryMetastases: Int?,
    val intervalTumorIncidenceRMetastasesStop: Int?,
)
