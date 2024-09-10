package com.hartwig.actin.personalization.similarity.population

import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.ir.Plot
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.step

object PfsPlot {
    private const val MIN_PATIENT_COUNT = 20
    private val percentageArray = (0..100 step 10).toList().map { it / 100.0 to "$it%" }.toTypedArray()

    fun createPfsPlot(sortedPopulationsByName: Map<String, List<DiagnosisAndEpisode>>): Plot? {
        val historiesByName = sortedPopulationsByName.mapValues { (_, tumors) -> PfsCalculation.eventHistory(tumors) }
            .filter { (_, histories) -> histories.size >= MIN_PATIENT_COUNT }

        return historiesByName.values.maxOfOrNull { it.last().daysSincePlanStart }?.let { longestInterval ->
            plot {
                step {
                    x(historiesByName.flatMap { (_, histories) -> histories.map(EventCountAndSurvivalAtTime::daysSincePlanStart) }) {
                        axis.breaksLabeled(*(0..longestInterval step 100).toList().map { it to "$it" }.toTypedArray())
                        axis.name = "Days since treatment start"
                    }
                    y(
                        historiesByName.flatMap { (_, histories) -> histories.map(EventCountAndSurvivalAtTime::survival) },
                        "PFS %"
                    ) {
                        axis.breaksLabeled(*percentageArray)
                    }
                    color(historiesByName.flatMap { (name, histories) -> histories.map { name } }, "Group")
                }
                layout.size = 800 to 400
            }
        }
    }
}
