package com.hartwig.actin.personalization.similarity.population

enum class MeasurementType(val calculation: Calculation) {
    TREATMENT_DECISION(TreatmentDecisionCalculation),
    PROGRESSION_FREE_SURVIVAL(PfsCalculation),
    PERCENT_WITH_PFS_THREE_MONTHS(PercentPfsWithDaysCalculation(91)),
    PERCENT_WITH_PFS_SIX_MONTHS(PercentPfsWithDaysCalculation(183)),
    PERCENT_WITH_PFS_ONE_YEAR(PercentPfsWithDaysCalculation(365))
}