package com.hartwig.actin.personalization.datamodel.treatment

import kotlinx.serialization.Serializable

typealias DrugScheme = List<DrugTreatment>

@Serializable
data class SystemicTreatment(
    val treatment: Treatment,
    val schemes: List<DrugScheme>,
)
