package com.hartwig.actin.personalization.similarity.report

import com.hartwig.actin.personalization.datamodel.Treatment

object PfsTable {

    fun pfsTable(
        patientsByTreatment: Map<Treatment, List<DiagnosisAndEpisode>>,
        columnDefinitions: List<Pair<String, (DiagnosisAndEpisode) -> Boolean>>
    ): TableContent {
        val sortedPatients = patientsByTreatment.entries.map { (treatment, patients) ->
            treatment to patients.filter { (_, episode) -> episode.systemicTreatmentPlan?.pfs != null }
        }
            .sortedByDescending { it.second.size }

        val entries = sortedPatients.map { (treatment, patients) ->
            val rowValues = columnDefinitions.map { (_, criteria) -> pfsForPopulation(patients.filter(criteria)) }
            listOf(TableElement.regular(treatment.display)) + rowValues
        }

        val filteredPatients = sortedPatients.flatMap { it.second }
        val dataLabels = columnDefinitions.map { (title, criteria) -> "$title (n=${filteredPatients.count(criteria)})" }
        return TableContent(
            "Progression-free survival (median (range)) in NCR real-world data set",
            listOf("Treatment") + dataLabels,
            entries
        )
    }

    private fun pfsForPopulation(population: List<DiagnosisAndEpisode>): TableElement {
        val filteredPopulation = population.mapNotNull { (_, episode) -> episode.systemicTreatmentPlan?.pfs }
        val medianPfs = median(filteredPopulation)
        val minPfs = filteredPopulation.minOrNull()
        val maxPfs = filteredPopulation.maxOrNull()

        return when {
            medianPfs.isNaN() -> {
                TableElement.regular("-")
            }

            filteredPopulation.size <= 5 -> {
                TableElement.regular("n≤5")
            }

            else -> {
                TableElement(medianPfs.toString(), " ($minPfs-$maxPfs) \n(n=${filteredPopulation.size})")
            }
        }
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