package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.DiagnosisEpisodeTreatment
import com.hartwig.actin.personalization.similarity.report.TableElement

val PFS_CALCULATION = SurvivalCalculation(
    timeFunction = { it.systemicTreatmentPlan?.observedPfsDays },
    eventFunction = { it.systemicTreatmentPlan?.hadProgressionEvent },
    title = "Progression-free survival (median, IQR) in NCR real-world data set"
)

val OS_CALCULATION = SurvivalCalculation(
    timeFunction = { it.diagnosis.observedOsFromTumorIncidenceDays },
    eventFunction = { it.diagnosis.hadSurvivalEvent },
    title = "Overall survival (median, IQR) in NCR real-world data set"
)

class SurvivalCalculation(
    internal val timeFunction: (DiagnosisEpisodeTreatment) -> Int?,
    internal val eventFunction: (DiagnosisEpisodeTreatment) -> Boolean?,
    internal val title: String
) : Calculation {

    private val MIN_PATIENT_COUNT = 20

    override fun isEligible(patient: DiagnosisEpisodeTreatment): Boolean {
        return eventFunction(patient) != null  && timeFunction(patient) != null
    }

    override fun calculate(patients: List<DiagnosisEpisodeTreatment>, eligiblePopulationSize: Int): Measurement {
        val survivalValues = patients.filter { isEligible(it) }
            .mapNotNull { timeFunction(it)?.toDouble() }
            .sorted()

        if (survivalValues.isEmpty()) {
            return Measurement(Double.NaN, 0, null, null, Double.NaN)
        }

        val median = calculateMedian(survivalValues)
        val iqr = calculateIQR(survivalValues)

        val min = survivalValues.firstOrNull()?.toInt()
        val max = survivalValues.lastOrNull()?.toInt()

        return Measurement(median, survivalValues.size, min,  max, iqr)
    }

    private fun calculateMedian(sortedValues: List<Double>): Double {
        val size = sortedValues.size
        return if (size % 2 == 1) {
            sortedValues[size / 2]
        } else {
            val mid1 = sortedValues[size / 2 - 1]
            val mid2 = sortedValues[size / 2]
            (mid1 + mid2) / 2.0
        }
    }

    private fun calculateIQR(sortedValues: List<Double>): Double {
        val size = sortedValues.size
        return if (size < 2) {
            0.0
        } else {
            val q1 = calculateMedian(sortedValues.subList(0, size / 2))
            val q3 = calculateMedian(sortedValues.subList((size + 1) / 2, size))
            q3 - q1
        }
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
        populationToProcess: List<DiagnosisEpisodeTreatment>,
        eventHistory: List<EventCountAndSurvivalAtTime> = emptyList()
    ): List<EventCountAndSurvivalAtTime> {
        return if (populationToProcess.isEmpty()) {
            eventHistory
        } else {
            val current = populationToProcess.first()
            val time = timeFunction(current)
            val eventOccurred = eventFunction(current)

            return if (time == null || eventOccurred != true) {
                eventHistory(populationToProcess.drop(1), eventHistory)
            } else {
                val previousEvent = eventHistory.lastOrNull() ?: EventCountAndSurvivalAtTime(0, 0, 1.0)
                val newEvent = EventCountAndSurvivalAtTime(
                    time,
                    previousEvent.numberOfEvents + 1,
                    previousEvent.survival * (1 - (1.0 / populationToProcess.size))
                )
                eventHistory(populationToProcess.drop(1), eventHistory + newEvent)
            }
        }
    }
}
