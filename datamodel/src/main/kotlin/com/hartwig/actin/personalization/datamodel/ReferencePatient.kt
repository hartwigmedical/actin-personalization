package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class ReferencePatient(
    val source: ReferenceSource,
    val sourceId: Int,
    val sex: Sex,
    val tumors: List<Tumor>
)
