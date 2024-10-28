package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.similarity.report.TableElement

object OsCalculation : Calculation {

    private const val MIN_PATIENT_COUNT = 20

    override fun isEligible(patient: DiagnosisAndEpisode) =
        patient.second.systemicTreatmentPlan?.observedOsFromTreatmentStartDays != null

    override fun calculate(patients: List<DiagnosisAndEpisode>, eligiblePopulationSize: Int): Measurement {
        val eventHistory = eventHistory(patients.sortedBy { it.second.systemicTreatmentPlan!!.observedOsFromTreatmentStartDays!! })

        return Measurement(
            osForQuartile(eventHistory, 0.5),
            patients.size,
            eventHistory.firstOrNull()?.daysSincePlanStart,
            eventHistory.lastOrNull()?.daysSincePlanStart,
            osForQuartile(eventHistory, 0.75) - osForQuartile(eventHistory, 0.25)
        )
    }

    private fun osForQuartile(eventHistory: List<EventCountAndSurvivalAtTime>, quartileAsDecimal: Double): Double {
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
        return "Overall survival (median, IQR) in NCR real-world data set"
    }

    tailrec fun eventHistory(
        populationToProcess: List<DiagnosisAndEpisode>,
        eventHistory: List<EventCountAndSurvivalAtTime> = emptyList(),
    ): List<EventCountAndSurvivalAtTime> {
        return if (populationToProcess.isEmpty()) eventHistory else {
            val treatmentDetails = populationToProcess.first().second.systemicTreatmentPlan!!
            val previousEvent = eventHistory.lastOrNull() ?: EventCountAndSurvivalAtTime(0, 0, 1.0)
            val newEventHistory = if (!treatmentDetails.hadSurvivalEvent!!) eventHistory else {
                val newEvent = EventCountAndSurvivalAtTime(
                    treatmentDetails.observedOsFromTreatmentStartDays!!,
                    previousEvent.numEvents + 1,
                    previousEvent.survival * (1 - (1.0 / populationToProcess.size))
                )
                eventHistory + newEvent
            }
            eventHistory(populationToProcess.drop(1), newEventHistory)
        }
    }
}
