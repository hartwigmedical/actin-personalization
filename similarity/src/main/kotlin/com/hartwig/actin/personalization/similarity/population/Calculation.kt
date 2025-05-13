package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.ReferenceEntry
import com.hartwig.actin.personalization.similarity.report.TableElement

interface Calculation {

    fun isEligible(entry: ReferenceEntry): Boolean

    fun calculate(entries: List<ReferenceEntry>, eligiblePopulationSize: Int): Measurement

    fun createTableElement(measurement: Measurement): TableElement

    fun title(): String
}