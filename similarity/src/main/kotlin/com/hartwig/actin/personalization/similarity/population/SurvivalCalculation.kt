package com.hartwig.actin.personalization.similarity.population
import com.hartwig.actin.personalization.datamodel.SystemicTreatmentPlan
import com.hartwig.actin.personalization.similarity.report.TableElement

class SurvivalCalculation(
    val timeFunction: (SystemicTreatmentPlan) -> Int?,
    val eventFunction: (SystemicTreatmentPlan) -> Boolean?,
    val title: String
) : Calculation {

    private val MIN_PATIENT_COUNT = 20

    override fun isEligible(patient: DiagnosisAndEpisode): Boolean {
        val systemicTreatmentPlan = patient.second.systemicTreatmentPlan ?: return false
        return timeFunction(systemicTreatmentPlan) != null && eventFunction(systemicTreatmentPlan) != null
    }

    override fun calculate(patients: List<DiagnosisAndEpisode>, eligiblePopulationSize: Int): Measurement {
        val sortedPatients = patients.mapNotNull { patient -> timeFunction(patient.second.systemicTreatmentPlan!!)?.let { patient to it } }
            .sortedBy { it.second }
            .map { it.first }

        val eventHistory = eventHistory(sortedPatients)

        return Measurement(
            survivalForQuartile(eventHistory, 0.5),
            patients.size,
            eventHistory.firstOrNull()?.daysSincePlanStart,
            eventHistory.lastOrNull()?.daysSincePlanStart,
            survivalForQuartile(eventHistory, 0.75) - survivalForQuartile(eventHistory, 0.25)
        )
    }

    private fun survivalForQuartile(eventHistory: List<EventCountAndSurvivalAtTime>, quartileAsDecimal: Double): Double {
        val expectedSurvivalFraction = 1 - quartileAsDecimal
        val searchIndex = eventHistory.binarySearchBy(-expectedSurvivalFraction) { -it.survival }
        val realIndex = if (searchIndex < 0) -(searchIndex + 1) else searchIndex

        return if (realIndex == eventHistory.size) Double.NaN else eventHistory[realIndex].daysSincePlanStart.toDouble()
    }

    override fun createTableElement(measurement: Measurement): TableElement {
        return when {
            measurement.numPatients <= MIN_PATIENT_COUNT -> {
                TableElement.regular("n≤$MIN_PATIENT_COUNT")
            }
            measurement.value.isNaN() -> {
                TableElement.regular("-")
            }
            else -> {
                with(measurement) {
                    val iqrString = if (iqr != null && !iqr.isNaN()) {
                        ", IQR: $iqr"
                    } else ""
                    TableElement(
                        value.toString(),
                        "${iqrString}\n(n=$numPatients)"
                    )
                }
            }
        }
    }

    override fun title(): String {
        return title
    }


    tailrec fun eventHistory(
        populationToProcess: List<DiagnosisAndEpisode>,
        eventHistory: List<EventCountAndSurvivalAtTime> = emptyList(),
    ): List<EventCountAndSurvivalAtTime> {
        if (populationToProcess.isEmpty()) return eventHistory

        val treatmentDetails = populationToProcess.first().second.systemicTreatmentPlan
        if (treatmentDetails == null) return eventHistory(populationToProcess.drop(1), eventHistory)

        // Retrieve time and event information, handling nulls
        val time = timeFunction(treatmentDetails)
        val eventOccurred = eventFunction(treatmentDetails)

        return if (time == null || eventOccurred != true) {
            // Skip if time or event status is null or event did not occur
            eventHistory(populationToProcess.drop(1), eventHistory)
        } else {
            val previousEvent = eventHistory.lastOrNull() ?: EventCountAndSurvivalAtTime(0, 0, 1.0)
            val newEvent = EventCountAndSurvivalAtTime(
                time,
                previousEvent.numEvents + 1,
                previousEvent.survival * (1 - (1.0 / populationToProcess.size))
            )
            eventHistory(populationToProcess.drop(1), eventHistory + newEvent)
        }
    }

}
