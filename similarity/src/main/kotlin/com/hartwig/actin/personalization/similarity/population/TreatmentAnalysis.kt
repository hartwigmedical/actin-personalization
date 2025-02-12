package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.treatment.TreatmentGroup

data class TreatmentAnalysis(
    val treatment: TreatmentGroup,
    val measurementsByTypeAndPopulationName: Map<MeasurementType, Map<String, Measurement>>,
)
