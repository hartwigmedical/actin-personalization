package com.hartwig.actin.personalization.similarity.report

import com.hartwig.actin.personalization.similarity.population.Calculation
import com.hartwig.actin.personalization.similarity.population.DiagnosisAndEpisode
import com.hartwig.actin.personalization.similarity.population.Measurement

object PfsCalculation : Calculation {
    override fun isEligible(patient: DiagnosisAndEpisode) = patient.second.systemicTreatmentPlan?.pfs != null

    override fun calculate(patients: List<DiagnosisAndEpisode>, eligibleSubPopulationSize: Int): Measurement {
        val pfsList = patients.mapNotNull { (_, episode) -> episode.systemicTreatmentPlan?.pfs }
        return Measurement(
            median(pfsList), pfsList.size, pfsList.minOrNull(), pfsList.maxOrNull()
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
                TableElement(measurement.value.toString(), " (${measurement.min}-${measurement.max}) \n(n=${measurement.numPatients})")
            }
        }
    }

    override fun title(): String {
        return "Progression-free survival (median (range)) in NCR real-world data set"
    }

    private fun median(list: List<Int>): Double {
        return when (list.size) {
            0 -> Double.NaN
            1 -> list.first().toDouble()
            else -> {
                val midPoint = list.size / 2
                list.sorted().let {
                    if (it.size % 2 == 0)
                        (it[midPoint] + it[midPoint - 1]) / 2.0
                    else
                        it[midPoint].toDouble()
                }
            }
        }
    }
}
