package com.hartwig.actin.personalization.datamodel.assessment

import kotlinx.serialization.Serializable

@Serializable
data class AsaAssessment(
    val daysSinceDiagnosis: Int,
    val asaClassification: AsaClassification
)