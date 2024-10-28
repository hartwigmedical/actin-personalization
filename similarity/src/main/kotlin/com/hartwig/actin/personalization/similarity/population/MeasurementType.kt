package com.hartwig.actin.personalization.similarity.population

enum class MeasurementType(val calculation: Calculation) {
    TREATMENT_DECISION(TreatmentDecisionCalculation),
    PROGRESSION_FREE_SURVIVAL(PfsCalculation),
    OVERALL_SURVIVAL(OsCalculation)
}