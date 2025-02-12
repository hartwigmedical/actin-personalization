package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.MetastasesSurgeryType
import com.hartwig.actin.personalization.datamodel.SurgeryRadicality
import kotlinx.serialization.Serializable

@Serializable
data class MetastaticSurgery (
    val daysSinceDiagnosis: Int?,
    val type: MetastasesSurgeryType,
    val radicality: SurgeryRadicality?
)
