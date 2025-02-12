package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.AsaClassification
import kotlinx.serialization.Serializable

@Serializable
data class AsaAssessment(
    val daysSinceDiagnosis: Int,
    val asaClassification: AsaClassification
)
