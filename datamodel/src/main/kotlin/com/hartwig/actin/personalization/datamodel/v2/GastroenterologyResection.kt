package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.GastroenterologyResectionType
import kotlinx.serialization.Serializable

@Serializable
data class GastroenterologyResection(
    val daysSinceDiagnosis: Int?,
    val resectionType: GastroenterologyResectionType
)
