package com.hartwig.actin.personalization.similarity.population

data class SubPopulation(
    val name: String,
    val patientsByMeasurementType: Map<MeasurementType, List<DiagnosisAndEpisode>>
)
