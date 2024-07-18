package com.hartwig.actin.personalization.similarity.report

import com.hartwig.actin.personalization.similarity.population.MeasurementType
import com.hartwig.actin.personalization.similarity.population.PersonalizedDataAnalysis
import java.lang.IllegalArgumentException

data class TableElement(val boldContent: String? = null, val content: String? = null) {
    companion object {
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
        fun fromPersonalizedDataAnalysis(
            personalizedDataAnalysis: PersonalizedDataAnalysis, measurementType: MeasurementType
        ): TableContent {
            val headers = listOf("") + personalizedDataAnalysis.populations.map {
                "${it.name} (n=${it.patientsByMeasurementType[measurementType]?.size})"
            }
            val rows = personalizedDataAnalysis.treatmentAnalyses.map { (treatment, measurements) ->
                val rowValues = personalizedDataAnalysis.populations.map { population ->
                    measurementType.calculation.createTableElement(measurements[measurementType]!![population.name]!!)
                }
                listOf(TableElement.regular(treatment.display)) + rowValues
            }
            return TableContent(measurementType.calculation.title(), headers, rows)
        }
    }
}
