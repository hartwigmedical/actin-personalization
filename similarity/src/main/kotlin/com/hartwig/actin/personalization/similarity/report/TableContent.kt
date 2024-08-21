package com.hartwig.actin.personalization.similarity.report

import com.hartwig.actin.personalization.similarity.population.MeasurementType
import com.hartwig.actin.personalization.similarity.population.PersonalizedDataAnalysis
import com.hartwig.actin.personalization.similarity.population.Population
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
        fun createForMeasurementType(
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

        fun createForPopulation(personalizedDataAnalysis: PersonalizedDataAnalysis, population: Population): TableContent {
            val headers = listOf("Treatment", "Percentage assigned", "% PFS > 3 mo.", "% PFS > 6 mo.", "% PFS > 12 mo.")
            val measurementTypes = listOf(
                MeasurementType.TREATMENT_DECISION,
                MeasurementType.PERCENT_WITH_PFS_THREE_MONTHS,
                MeasurementType.PERCENT_WITH_PFS_SIX_MONTHS,
                MeasurementType.PERCENT_WITH_PFS_ONE_YEAR
            )
            val rows = personalizedDataAnalysis.treatmentAnalyses.map { (treatment, measurements) ->
                val rowValues = measurementTypes.map { measurementType ->
                    measurementType.calculation.createTableElement(measurements[measurementType]!![population.name]!!)
                }
                listOf(TableElement.regular(treatment.display)) + rowValues
            }
            return TableContent("${population.name} patient outcomes in NCR real-world data set", headers, rows)
        }
    }
}
