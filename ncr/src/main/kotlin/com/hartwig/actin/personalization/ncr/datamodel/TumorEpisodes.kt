package com.hartwig.actin.personalization.ncr.datamodel

data class TumorEpisodes(
    val diagnosis: Episode,
    val followups: List<Episode>
)
