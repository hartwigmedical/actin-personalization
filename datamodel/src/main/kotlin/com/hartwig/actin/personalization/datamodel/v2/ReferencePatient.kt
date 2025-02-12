package com.hartwig.actin.personalization.datamodel.v2

import kotlinx.serialization.Serializable

@Serializable
data class ReferencePatient(
    val sex: Sex,
    val tumors: List<Tumor>
)
