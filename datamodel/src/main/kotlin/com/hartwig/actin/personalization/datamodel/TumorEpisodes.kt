package com.hartwig.actin.personalization.datamodel

data class TumorEpisodes(
    val diagnosis: Diagnosis,
    val diagnosisEpisode: Episode,
    val followupEpisodes: List<Episode>
)
