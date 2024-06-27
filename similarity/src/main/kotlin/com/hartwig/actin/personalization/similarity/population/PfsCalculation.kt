package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.similarity.report.TableElement
import kotlin.math.min

object PfsCalculation : Calculation {

    private const val MIN_PATIENT_COUNT = 20
    
    override fun isEligible(patient: DiagnosisAndEpisode) = patient.second.systemicTreatmentPlan?.pfs != null

    override fun calculate(patients: List<DiagnosisAndEpisode>, eligibleSubPopulationSize: Int): Measurement {
        val pfsList = patients.mapNotNull { (_, episode) -> episode.systemicTreatmentPlan?.pfs }.sorted()
        val (q1, q3) = if (pfsList.isEmpty()) Pair(Double.NaN, Double.NaN) else {
            val midPoint = pfsList.size / 2 + 1
            Pair(
                median(pfsList.subList(0, midPoint)),
                median(pfsList.subList(min(midPoint, pfsList.size), pfsList.size))
            )
        }
        return Measurement(
            median(pfsList), pfsList.size, pfsList.minOrNull(), pfsList.maxOrNull(), q3 - q1
        )
    }

    override fun createTableElement(measurement: Measurement): TableElement {
        return when {
            measurement.value.isNaN() -> {
                TableElement.regular("-")
            }

            measurement.numPatients <= MIN_PATIENT_COUNT -> {
                TableElement.regular("n≤$MIN_PATIENT_COUNT")
            }

            else -> {
                with(measurement) {
                    val iqrString = if (iqr != null && !iqr.isNaN()) {
                        "IQR: $iqr, "
                    } else ""
                    TableElement(
                        value.toString(),
                        "\n(${iqrString}n=$numPatients)"
                    )
                }
            }
        }
    }

    override fun title(): String {
        return "Progression-free survival (median (range)) in NCR real-world data set"
    }

    private fun median(sortedList: List<Int>): Double {
        return when (sortedList.size) {
            0 -> Double.NaN
            1 -> sortedList.first().toDouble()
            else -> {
                val midPoint = sortedList.size / 2
                sortedList.let {
                    if (it.size % 2 == 0)
                        (it[midPoint] + it[midPoint - 1]) / 2.0
                    else
                        it[midPoint].toDouble()
                }
            }
        }
    }
}
