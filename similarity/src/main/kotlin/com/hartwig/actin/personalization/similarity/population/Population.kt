package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.Tumor

data class Population(
    val name: String,
    val tumorsByMeasurementType: Map<MeasurementType, List<Tumor>>
)
