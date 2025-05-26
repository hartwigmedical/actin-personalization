package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.ReferenceEntry

data class Population(
    val name: String,
    val entriesByMeasurementType: Map<MeasurementType, List<ReferenceEntry>>
)
