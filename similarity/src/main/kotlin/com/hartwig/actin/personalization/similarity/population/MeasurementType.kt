package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.similarity.report.PfsCalculation
import com.hartwig.actin.personalization.similarity.report.TreatmentDecisionCalculation

enum class MeasurementType(val calculation: Calculation) {
    TREATMENT_DECISION(TreatmentDecisionCalculation),
    PROGRESSION_FREE_SURVIVAL(PfsCalculation)
}