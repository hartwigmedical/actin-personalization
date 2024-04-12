package com.hartwig.actin.personalization.datamodel

data class TumorEpisodes(
    val diagnosisEpisode: DiagnosisEpisode,
    val followupEpisodes: List<FollowupEpisode>
)
