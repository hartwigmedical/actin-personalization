package com.hartwig.actin.personalization.datamodel

data class TumorEntry(
    val diagnosis: Diagnosis,
    val episodes: List<Episode>
)
