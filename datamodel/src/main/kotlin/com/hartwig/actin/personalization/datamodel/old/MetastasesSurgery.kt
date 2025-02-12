package com.hartwig.actin.personalization.datamodel.old

import com.hartwig.actin.personalization.datamodel.v2.treatment.MetastasesSurgeryType
import com.hartwig.actin.personalization.datamodel.v2.treatment.SurgeryRadicality
import kotlinx.serialization.Serializable

@Serializable
data class MetastasesSurgery(
    val metastasesSurgeryType: MetastasesSurgeryType,
    val surgeryMetastasesRadicality: SurgeryRadicality?,
    val intervalTumorIncidenceMetastasesSurgeryDays: Int?
)