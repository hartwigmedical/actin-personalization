package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.old.DiagnosisEpisode
import com.hartwig.actin.personalization.similarity.report.TableElement

val PFS_CALCULATION = SurvivalCalculation(
    timeFunction = { it.episode.systemicTreatmentPlan!!.observedPfsDays },
    eventFunction = { it.episode.systemicTreatmentPlan!!.hadProgressionEvent },
    title = "Progression-free survival (median, IQR) in NCR real-world data set"
)

val OS_CALCULATION = SurvivalCalculation(
    timeFunction = { it.episode.systemicTreatmentPlan!!.observedOsFromTreatmentStartDays },
    eventFunction = { it.diagnosis.hadSurvivalEvent },
    title = "Overall survival (median, IQR) in NCR real-world data set"
)

class SurvivalCalculation(
    internal val timeFunction: (DiagnosisEpisode) -> Int?,
    private val eventFunction: (DiagnosisEpisode) -> Boolean?,
    private val title: String
) : Calculation {

    private val MIN_PATIENT_COUNT = 20

    override fun isEligible(patient: DiagnosisEpisode): Boolean {
        return eventFunction(patient) != null  && timeFunction(patient) != null
    }

    override fun calculate(patients: List<DiagnosisEpisode>, eligiblePopulationSize: Int): Measurement {
        val eligiblePatients = patients.filter { isEligible(it) }
        val eventHistory = buildEventHistory(eligiblePatients.sortedBy(timeFunction))

        if (eventHistory.isEmpty()) {
            return Measurement(Double.NaN, 0, null, null, Double.NaN)
        }

        return Measurement(
            survivalForQuartile(eventHistory, 0.5),
            eligiblePatients.size,
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

    tailrec fun buildEventHistory(
        populationToProcess: List<DiagnosisEpisode>,
        eventHistory: List<EventCountAndSurvivalAtTime> = emptyList(),
    ): List<EventCountAndSurvivalAtTime> {
        if (populationToProcess.isEmpty()) return eventHistory

        val currentPatient = populationToProcess.first()
        val time = timeFunction(currentPatient)!!
        val eventOccurred = eventFunction(currentPatient)!!

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
