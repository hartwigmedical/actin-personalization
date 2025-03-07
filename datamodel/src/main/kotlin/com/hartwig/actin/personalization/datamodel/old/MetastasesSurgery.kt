package com.hartwig.actin.personalization.datamodel.old

import com.hartwig.actin.personalization.datamodel.treatment.MetastaticSurgeryType
import com.hartwig.actin.personalization.datamodel.treatment.SurgeryRadicality
import kotlinx.serialization.Serializable

@Serializable
data class MetastasesSurgery(
    val metastasesSurgeryType: MetastaticSurgeryType,
    val surgeryMetastasesRadicality: SurgeryRadicality?,
    val intervalTumorIncidenceMetastasesSurgeryDays: Int?
)