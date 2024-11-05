package com.hartwig.actin.personalization.similarity.population

data class EventCountAndSurvivalAtTime(
    val daysSinceTreatmentPlanStart: Int,
    val numberOfEvents: Int,
    val survival: Double
)

