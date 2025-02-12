package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.AsaClassification

data class AsaAssessment(
    val daysSinceDiagnosis: Int,
    val asaClassification: AsaClassification
)
