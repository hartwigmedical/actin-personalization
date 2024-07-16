package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class GastroenterologyResection(
    val gastroenterologyResectionType: GastroenterologyResectionType,
    val intervalTumorIncidenceGastroenterologyResection: Int?
)
