package com.hartwig.actin.personalization.datamodel

import com.hartwig.actin.personalization.datamodel.v2.treatment.GastroenterologyResectionType
import kotlinx.serialization.Serializable

@Serializable
data class GastroenterologyResection(
    val gastroenterologyResectionType: GastroenterologyResectionType,
    val intervalTumorIncidenceGastroenterologyResection: Int?
)
