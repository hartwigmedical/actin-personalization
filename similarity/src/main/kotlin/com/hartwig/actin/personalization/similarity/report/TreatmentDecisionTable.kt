package com.hartwig.actin.personalization.similarity.report

import com.hartwig.actin.personalization.datamodel.Treatment

object TreatmentDecisionTable {

    fun decisionTable(
        patientsByTreatment: Map<Treatment, List<DiagnosisAndEpisode>>,
        columnDefinitions: List<Pair<String, (DiagnosisAndEpisode) -> Boolean>>
    ): TableContent {
        val allPatients = patientsByTreatment.flatMap { it.value }
        val sortedPatients = patientsByTreatment.entries.sortedByDescending { it.value.size }
        val subPopulationSizes = columnDefinitions.associate { (title, criteria) -> title to allPatients.count(criteria) }

        val entries = sortedPatients.map { (treatment, patients) ->
            val rowValues = columnDefinitions.map { (title, criteria) ->
                treatmentDecisionForSubPopulation(patients.count(criteria), subPopulationSizes[title]!!)
            }
            listOf(treatment.display.toString()) + rowValues
        }

        val dataLabels = columnDefinitions.map { (title, _) -> "$title (n=${subPopulationSizes[title]!!})" }
        return TableContent("Treatment decisions in real-world data set (NCR)", listOf("Treatment") + dataLabels, entries)
    }

    fun treatmentDecisionForSubPopulation(filteredSubPopulationSize: Int, subPopulationSize: Int): String {
        return String.format("%.1f%%", 100.0 * filteredSubPopulationSize / subPopulationSize)
    }
}