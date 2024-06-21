package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.Treatment

data class TreatmentMeasurementCollection(val measurementsByTreatment: Map<Treatment, Measurement>, val numPatients: Int)

data class SubPopulationAnalysis(
    val name: String,
    val treatmentMeasurements: Map<MeasurementType, TreatmentMeasurementCollection>,
    val treatments: List<Treatment>
)
