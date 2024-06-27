package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.TreatmentGroup

data class TreatmentMeasurementCollection(val measurementsByTreatment: Map<TreatmentGroup, Measurement>, val numPatients: Int)

data class SubPopulationAnalysis(
    val name: String,
    val treatmentMeasurements: Map<MeasurementType, TreatmentMeasurementCollection>,
    val treatments: List<TreatmentGroup>
)
