package com.hartwig.actin.personalization.ncr.datamodel

data class PatientRecord(
    val id: Int,
    val episodesPerTumor: Map<Tumor, TumorEpisodes>
)