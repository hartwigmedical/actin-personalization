package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.similarity.report.TableElement
import kotlin.collections.first
import kotlin.collections.mapNotNull
import kotlin.collections.maxOrNull
import kotlin.collections.minOrNull
import kotlin.collections.sorted
import kotlin.isNaN
import kotlin.let
import kotlin.math.min

object PfsCalculation : Calculation {
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

            measurement.numPatients <= 5 -> {
                TableElement.regular("n≤5")
            }

            else -> {
                with(measurement) {
                    val iqrString = if (iqr != null && iqr != Double.NaN) {
                        "IQR: $iqr, "
                    } else ""
                    TableElement(
                        value.toString(),
                        " (r: $min-$max)\n(${iqrString}n=$numPatients)"
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
