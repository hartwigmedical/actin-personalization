package com.hartwig.actin.personalization.ncr.datamodel

data class PatientRecord(
    val id: Int,
    val sex: Sex,
    val vitalStatus: VitalStatus,
    val episodesPerTumorOfInterest: Map<TumorOfInterest, TumorEpisodes>,
    val previousTumors: List<TumorPrior>
)