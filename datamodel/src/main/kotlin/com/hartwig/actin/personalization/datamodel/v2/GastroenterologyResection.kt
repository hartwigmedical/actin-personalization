package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.GastroenterologyResectionType

data class GastroenterologyResection(
    val daysSinceDiagnosis: Int?,
    val resectionType: GastroenterologyResectionType
)
