package com.hartwig.actin.personalization.ncr.datamodel

data class TumorEpisodes(
    val diagnosis: Episode, //do we need specific DiagnosisEpisode and List<FollowUpEpisodes> ? Some variables are never collected in follow-up.
    val followups: List<Episode>
)
