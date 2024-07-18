package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class ReferencePatient(
    val ncrId: Int,
    val sex: Sex,
    val isAlive: Boolean,
    val tumorEntries: List<TumorEntry>
)