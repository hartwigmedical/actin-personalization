package com.hartwig.actin.personalization.datamodel.v2.treatment

import kotlinx.serialization.Serializable

@Serializable
data class GastroenterologyResection(
    val daysSinceDiagnosis: Int?,
    val resectionType: GastroenterologyResectionType
)
