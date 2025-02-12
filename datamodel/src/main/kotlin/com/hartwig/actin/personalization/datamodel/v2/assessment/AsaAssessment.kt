package com.hartwig.actin.personalization.datamodel.v2.assessment

import kotlinx.serialization.Serializable

@Serializable
data class AsaAssessment(
    val daysSinceDiagnosis: Int,
    val asaClassification: AsaClassification
)