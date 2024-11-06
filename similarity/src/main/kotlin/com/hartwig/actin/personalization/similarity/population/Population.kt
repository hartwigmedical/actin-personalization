package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.DiagnosisEpisode

data class Population(
    val name: String,
    val patientsByMeasurementType: Map<MeasurementType, List<DiagnosisEpisode>>
)
