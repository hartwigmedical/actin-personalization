package com.hartwig.actin.personalization.similarity.report

import com.hartwig.actin.personalization.similarity.population.MeasurementType
import com.hartwig.actin.personalization.similarity.population.SubPopulationAnalysis
import java.lang.IllegalArgumentException

data class TableElement(val boldContent: String? = null, val content: String? = null) {
    companion object {
        fun bold(content: String) = TableElement(boldContent = content)

        fun regular(content: String) = TableElement(content = content)
    }
}

data class TableContent(val title: String, val headers: List<String>, val rows: List<List<TableElement>>, val sizes: List<Float>? = null) {

    fun check() {
        if (sizes?.let { it.size == headers.size } == false) {
            throw IllegalArgumentException("Sizes must have the same number of elements as the headers")
        }
        if (rows.any { it.size != headers.size }) {
            throw IllegalArgumentException("All rows must have the same number of columns as the headers")
        }
    }

    companion object {
        fun fromSubPopulationAnalyses(
            subPopulationAnalyses: List<SubPopulationAnalysis>,
            measurementType: MeasurementType,
        ): TableContent {
            val headers = listOf("") + subPopulationAnalyses.map {
                "${it.name} (n=${it.treatmentMeasurements[measurementType]?.numPatients})"
            }
            val measurementsBySubPopulationName = subPopulationAnalyses.associate { (name, treatmentMeasurements) ->
                name to treatmentMeasurements[measurementType]!!.measurementsByTreatment
            }
            val rows = subPopulationAnalyses.first().treatments.map { treatment ->
                val rowValues = subPopulationAnalyses.map { subPopulation ->
                    measurementType.calculation.createTableElement(measurementsBySubPopulationName[subPopulation.name]!![treatment]!!)
                }
                listOf(TableElement.regular(treatment.display)) + rowValues
            }
            return TableContent(measurementType.calculation.title(), headers, rows)
        }
    }
}
