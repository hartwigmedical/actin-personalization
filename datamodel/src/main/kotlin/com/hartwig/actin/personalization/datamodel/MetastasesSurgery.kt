package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class MetastasesSurgery(
    val metastasesSurgeryType: MetastasesSurgeryType,
    val surgeryMetastasesRadicality: SurgeryRadicality?,
    val intervalTumorIncidenceMetastasesSurgeryDays: Int?
)
