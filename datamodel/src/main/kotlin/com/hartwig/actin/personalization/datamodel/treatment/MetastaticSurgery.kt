package com.hartwig.actin.personalization.datamodel.treatment

import kotlinx.serialization.Serializable

@Serializable
data class MetastaticSurgery (
    val daysSinceDiagnosis: Int?,
    val type: MetastasesSurgeryType,
    val radicality: SurgeryRadicality?
)
