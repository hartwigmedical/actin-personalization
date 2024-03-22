package com.hartwig.actin.personalization.ncr.datamodel

data class PatientRecord(
    val id: Int,
    val episodesPerTumor: Map<TumorOfInterest, TumorEpisodes>,
    val sex: Sex,
    val vitalStatus: VitalStatus,
    val previousTumors: List<TumorPrior>
)