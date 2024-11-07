package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.DiagnosisEpisode
import com.hartwig.actin.personalization.similarity.report.TableElement

val PFS_CALCULATION = SurvivalCalculation(
    timeFunction = { it.episode.systemicTreatmentPlan?.observedPfsDays },
    eventFunction = { it.episode.systemicTreatmentPlan?.hadProgressionEvent },
    title = "Progression-free survival (median, IQR) in NCR real-world data set"
)

val OS_CALCULATION = SurvivalCalculation(
    timeFunction = { it.diagnosis.observedOsFromTumorIncidenceDays },
    eventFunction = { it.diagnosis.hadSurvivalEvent },
    title = "Overall survival (median, IQR) in NCR real-world data set"
)

class SurvivalCalculation(
    internal val timeFunction: (DiagnosisEpisode) -> Int?,
    internal val eventFunction: (DiagnosisEpisode) -> Boolean?,
    internal val title: String
) : Calculation {

    private val MIN_PATIENT_COUNT = 20

    override fun isEligible(patient: DiagnosisEpisode): Boolean {
        return eventFunction(patient) != null  && timeFunction(patient) != null
    }

    override fun calculate(patients: List<DiagnosisEpisode>, eligiblePopulationSize: Int): Measurement {

        val eventHistory = eventHistory(patients.sortedBy { timeFunction(it)})

        if (eventHistory.isEmpty()) {
            return Measurement(Double.NaN, 0, null, null, Double.NaN)
        }

        return Measurement(
            survivalForQuartile(eventHistory, 0.5),
            eventHistory.size,
            eventHistory.firstOrNull()?.daysSinceStartMeasurement,
            eventHistory.lastOrNull()?.daysSinceStartMeasurement,
            survivalForQuartile(eventHistory, 0.75) - survivalForQuartile(eventHistory, 0.25))
    }

    private fun survivalForQuartile(eventHistory: List<EventCountAndSurvivalAtTime>, quartileAsDecimal: Double): Double {
        val expectedSurvivalFraction = 1 - quartileAsDecimal
        val searchIndex = eventHistory.binarySearchBy(-expectedSurvivalFraction) { -it.survival }
        val realIndex = if (searchIndex < 0) -(searchIndex + 1) else searchIndex

        return if (realIndex == eventHistory.size) Double.NaN else eventHistory[realIndex].daysSinceStartMeasurement.toDouble()
    }


    override fun createTableElement(measurement: Measurement): TableElement {
        return when {
            measurement.numPatients <= MIN_PATIENT_COUNT -> TableElement.regular("n≤$MIN_PATIENT_COUNT")
            measurement.value.isNaN() -> TableElement.regular("-")
            else -> with(measurement) {
                val iqrString = if (iqr != null && !iqr.isNaN()) ", IQR: $iqr" else ""
                TableElement(value.toString(), "${iqrString}\n(n=$numPatients)")
            }
        }
    }

    override fun title(): String {
        return title
    }
    tailrec fun eventHistory(
        populationToProcess: List<DiagnosisEpisode>,
        eventHistory: List<EventCountAndSurvivalAtTime> = emptyList(),
    ): List<EventCountAndSurvivalAtTime> {
        if (populationToProcess.isEmpty()) {
            return eventHistory
        }

        val current = populationToProcess.first()
        val eventOccurred = eventFunction(current)
        val time = timeFunction(current)

        return if (eventOccurred == null || time == null) {
            eventHistory(populationToProcess.drop(1), eventHistory)
        } else {
            val previousEvent = eventHistory.lastOrNull() ?: EventCountAndSurvivalAtTime(0, 0, 1.0)

            val newEvent = EventCountAndSurvivalAtTime(
                daysSinceStartMeasurement = time,
                numberOfEvents = if (eventOccurred) previousEvent.numberOfEvents + 1 else previousEvent.numberOfEvents,
                survival = if (eventOccurred) {
                    previousEvent.survival * (1 - (1.0 / populationToProcess.size))
                } else {
                    previousEvent.survival
                }
            )

            eventHistory(populationToProcess.drop(1), eventHistory + newEvent)
        }
    }

}
