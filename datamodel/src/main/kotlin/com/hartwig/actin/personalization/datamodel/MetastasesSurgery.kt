package com.hartwig.actin.personalization.datamodel

data class MetastasesSurgery(
    val metastasesSurgeryType: MetastasesSurgeryType,
    val surgeryMetastasesRadicality: SurgeryRadicality?,
    val intervalTumorIncidenceSurgeryMetastases: Int?,
    val intervalTumorIncidenceRMetastasesStop: Int?,
)
