package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.SystemicTreatmentPlan

enum class MeasurementType(val calculation: Calculation) {
    TREATMENT_DECISION(TreatmentDecisionCalculation),
    PROGRESSION_FREE_SURVIVAL(PFS_CALCULATION),
    OVERALL_SURVIVAL(OS_CALCULATION)
}