package com.hartwig.actin.personalization.similarity.population

data class EventCountAndSurvivalAtTime(
    val daysSinceStart: Int,
    val numberOfEvents: Int,
    val survival: Double
)

