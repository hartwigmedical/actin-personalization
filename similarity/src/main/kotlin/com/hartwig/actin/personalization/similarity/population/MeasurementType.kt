package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.SystemicTreatmentPlan

enum class MeasurementType(val calculation: Calculation) {
    TREATMENT_DECISION(TreatmentDecisionCalculation),
    PROGRESSION_FREE_SURVIVAL(SurvivalCalculation(timeFunction = SystemicTreatmentPlan::observedPfsDays, eventFunction = SystemicTreatmentPlan::hadProgressionEvent, title = "Progression-free survival (median, IQR) in NCR real-world data set")),
    OVERALL_SURVIVAL(SurvivalCalculation(timeFunction = SystemicTreatmentPlan::observedOsFromTreatmentStartDays, eventFunction = SystemicTreatmentPlan::hadSurvivalEvent, title = "Overall survival (median, IQR) in NCR real-world data set"))
}