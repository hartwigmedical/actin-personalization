package com.hartwig.actin.personalization.ncr.datamodel

data class PatientRecord(
    val id: Int,
    val episodesPerTumor: Map<Tumor, TumorEpisodes>,
    val sex: Sex, // is determined on tumor level, but should be patient. I suppose we will map/extract the right value according to rules?
    val vitalStatus: VitalStatus // is determined on tumor level, but should be patient.
)