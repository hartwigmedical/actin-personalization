package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.DiagnosisEpisodeTreatment

data class Population(
    val name: String,
    val patientsByMeasurementType: Map<MeasurementType, List<DiagnosisEpisodeTreatment>>
)
