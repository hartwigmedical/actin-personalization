package com.hartwig.actin.personalization.similarity.population

import org.jetbrains.kotlinx.kandy.ir.Plot

data class PersonalizedDataAnalysis(
    val treatmentAnalyses: List<TreatmentAnalysis>,
    val populations: List<Population>,
    val plots: Map<String, Plot>
)

