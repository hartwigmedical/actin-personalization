package com.hartwig.actin.personalization.datamodel.old

import kotlinx.serialization.Serializable

@Serializable
data class TumorEntry(
    val diagnosis: Diagnosis,
    val episodes: List<Episode>
)