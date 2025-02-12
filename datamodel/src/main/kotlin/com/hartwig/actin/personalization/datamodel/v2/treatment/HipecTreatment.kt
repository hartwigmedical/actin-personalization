package com.hartwig.actin.personalization.datamodel.v2.treatment

import kotlinx.serialization.Serializable

@Serializable
data class HipecTreatment (
    val daysSinceDiagnosis: Int? = null,
    val hasHadHipecTreatment: Boolean,
)
