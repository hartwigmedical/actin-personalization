package com.hartwig.actin.personalization.datamodel.treatment

import kotlinx.serialization.Serializable

@Serializable
data class HipecTreatment (
    val daysSinceDiagnosis: Int,
)
