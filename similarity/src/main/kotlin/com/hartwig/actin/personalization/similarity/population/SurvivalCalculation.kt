package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.ReferenceEntry
import com.hartwig.actin.personalization.selection.ProgressionSelection
import com.hartwig.actin.personalization.similarity.report.TableElement

val PFS_CALCULATION = SurvivalCalculation(
    timeFunction = {
        val progression = ProgressionSelection.firstProgressionAfterSystemicTreatmentStart(it)
        if (progression != null) progression.daysSinceDiagnosis else 0
    },
    eventFunction = { ProgressionSelection.firstProgressionAfterSystemicTreatmentStart(it) != null },
    title = "Progression-free survival (median, IQR) in NCR real-world data set"
)

val OS_CALCULATION = SurvivalCalculation(
    timeFunction = { it.latestSurvivalMeasurement.daysSinceDiagnosis },
    eventFunction = { !it.latestSurvivalMeasurement.isAlive },
    title = "Overall survival (median, IQR) in NCR real-world data set"
)

class SurvivalCalculation(
    internal val timeFunction: (ReferenceEntry) -> Int?,
    private val eventFunction: (ReferenceEntry) -> Boolean?,
    private val title: String
) : Calculation {

    private val minEntryCount = 20

    override fun isEligible(entry: ReferenceEntry): Boolean {
        return eventFunction(entry) != null && timeFunction(entry) != null
    }

    override fun calculate(entries: List<ReferenceEntry>, eligiblePopulationSize: Int): Measurement {
        val eligibleEntries = entries.filter { isEligible(it) }
        val eventHistory = buildEventHistory(eligibleEntries.sortedBy(timeFunction))

        if (eventHistory.isEmpty()) {
            return Measurement(Double.NaN, 0, null, null, Double.NaN)
        }

        return Measurement(
            survivalForQuartile(eventHistory, 0.5),
            eligibleEntries.size,
            eventHistory.firstOrNull()?.daysSinceStart,
            eventHistory.lastOrNull()?.daysSinceStart,
            survivalForQuartile(eventHistory, 0.75) - survivalForQuartile(eventHistory, 0.25)
        )
    }

    private fun survivalForQuartile(eventHistory: List<EventCountAndSurvivalAtTime>, quartileAsDecimal: Double): Double {
        val expectedSurvivalFraction = 1 - quartileAsDecimal
        val searchIndex = eventHistory.binarySearchBy(-expectedSurvivalFraction) { -it.survival }
        val realIndex = if (searchIndex < 0) -(searchIndex + 1) else searchIndex

        return if (realIndex == eventHistory.size) Double.NaN else eventHistory[realIndex].daysSinceStart.toDouble()
    }
    
    override fun createTableElement(measurement: Measurement): TableElement {
        return when {
            measurement.numEntries <= minEntryCount -> TableElement.regular("n≤$minEntryCount")
            measurement.value.isNaN() -> TableElement.regular("-")
            else -> with(measurement) {
                val iqrString = if (iqr != null && !iqr.isNaN()) ", IQR: $iqr" else ""
                TableElement(value.toString(), "${iqrString}\n(n=$numEntries)")
            }
        }
    }

    override fun title(): String {
        return title
    }

    tailrec fun buildEventHistory(
        populationToProcess: List<ReferenceEntry>,
        eventHistory: List<EventCountAndSurvivalAtTime> = emptyList(),
    ): List<EventCountAndSurvivalAtTime> {
        if (populationToProcess.isEmpty()) return eventHistory

        val currentEntry = populationToProcess.first()
        val time = timeFunction(currentEntry)!!
        val eventOccurred = eventFunction(currentEntry)!!

        val previousEvent = eventHistory.lastOrNull() ?: EventCountAndSurvivalAtTime(0, 0, 1.0)
        val populationSize = populationToProcess.size

        val newEventHistory = if (eventOccurred) {
            val newEvent = EventCountAndSurvivalAtTime(
                daysSinceStart = time,
                numberOfEvents = previousEvent.numberOfEvents + 1,
                survival = previousEvent.survival * (1 - (1.0 / populationSize))
            )
            eventHistory + newEvent
        } else {
            eventHistory
        }
        return buildEventHistory(populationToProcess.drop(1), newEventHistory)
    }
}
