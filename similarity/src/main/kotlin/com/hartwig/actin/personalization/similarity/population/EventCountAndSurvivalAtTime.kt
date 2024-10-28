package com.hartwig.actin.personalization.similarity.population

data class EventCountAndSurvivalAtTime(
    val daysSincePlanStart: Int,
    val numEvents: Int,
    val survival: Double
)

