package com.hartwig.actin.personalization.datamodel.old

import com.hartwig.actin.personalization.datamodel.treatment.GastroenterologyResectionType
import kotlinx.serialization.Serializable

@Serializable
data class GastroenterologyResection(
    val gastroenterologyResectionType: GastroenterologyResectionType,
    val intervalTumorIncidenceGastroenterologyResection: Int?
)