package com.hartwig.actin.personalization.datamodel

data class PatientRecord(
    val ncrId: Int,
    val sex: Sex,
    val isAlive: Boolean?,
    val episodesPerTumorOfInterest: Map<TumorOfInterest, TumorEpisodes>,
    val priorTumors: List<TumorPrior>
)