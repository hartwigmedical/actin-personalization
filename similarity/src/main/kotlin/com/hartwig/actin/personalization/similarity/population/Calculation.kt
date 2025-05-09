package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.Tumor
import com.hartwig.actin.personalization.similarity.report.TableElement

interface Calculation {

    fun isEligible(entry: Tumor): Boolean

    fun calculate(entries: List<Tumor>, eligiblePopulationSize: Int): Measurement

    fun createTableElement(measurement: Measurement): TableElement

    fun title(): String
}