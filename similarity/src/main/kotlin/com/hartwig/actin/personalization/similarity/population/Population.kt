package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.old.DiagnosisEpisode

data class Population(
    val name: String,
    val patientsByMeasurementType: Map<MeasurementType, List<DiagnosisEpisode>>
)
