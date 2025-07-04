package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.ReferenceEntry

import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.ir.Plot
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.step

object SurvivalPlot {
    
    private const val MIN_ENTRY_COUNT = 20
    
    private val percentageArray = (0..100 step 10).map { it / 100.0 to "$it%" }.toTypedArray()

    fun createSurvivalPlot(
        sortedPopulationsByName: Map<String, List<ReferenceEntry>>,
        calculation: SurvivalCalculation,
        yAxisLabel: String
    ): Plot? {
        val historiesByName = sortedPopulationsByName.mapValues { (_, entries) ->
            val eligibleEntries = entries.filter(calculation::isEligible)
            calculation.buildEventHistory(eligibleEntries)
        }.filter { (_, histories) ->
            histories.size >= MIN_ENTRY_COUNT
        }

        return historiesByName.values.maxOfOrNull { it.last().daysSinceStart }?.let { longestInterval ->
            plot {
                step {
                    val xValues = historiesByName.flatMap { (_, histories) ->
                        histories.map(EventCountAndSurvivalAtTime::daysSinceStart)
                    }
                    val yValues = historiesByName.flatMap { (_, histories) ->
                        histories.map(EventCountAndSurvivalAtTime::survival)
                    }
                    val groups = historiesByName.flatMap { (name, histories) ->
                        histories.map { name }
                    }

                    x(xValues) {
                        axis.breaksLabeled(*(0..longestInterval step 100).map { it to "$it" }.toTypedArray())
                        axis.name = "Days since treatment start"
                    }
                    y(yValues, yAxisLabel) {
                        axis.breaksLabeled(*percentageArray)
                    }
                    color(groups, "Group")
                }
                layout.size = 1000 to 600
            }
        }
    }
}
