package com.hartwig.actin.analysis.ncr.datamodel

data class PatientRecord(
    val id: Int,
    val episodesPerTumor: Map<Tumor, List<Episode>>
)