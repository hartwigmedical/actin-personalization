package com.hartwig.actin.personalization.ncr.datamodel

data class TumorEpisodes(
    val diagnosisEpisode: DiagnosisEpisode,
    val followupEpisodes: List<FollowupEpisode>
)
