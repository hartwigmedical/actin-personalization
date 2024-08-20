package com.hartwig.actin.personalization.similarity.population

enum class MeasurementType(val calculation: Calculation) {
    TREATMENT_DECISION(TreatmentDecisionCalculation),
    PROGRESSION_FREE_SURVIVAL(PfsCalculation),
    PERCENT_WITH_PFS_ONE_YEAR(PercentPfsWithDaysCalculation(365))
}