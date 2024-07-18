package com.hartwig.actin.personalization.similarity.population

data class Population(
    val name: String,
    val patientsByMeasurementType: Map<MeasurementType, List<DiagnosisAndEpisode>>
)
