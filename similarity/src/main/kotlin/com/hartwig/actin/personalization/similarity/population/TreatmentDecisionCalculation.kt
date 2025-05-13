package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.ReferenceEntry
import com.hartwig.actin.personalization.similarity.report.TableElement
import com.hartwig.actin.personalization.similarity.report.percentage

object TreatmentDecisionCalculation : Calculation {

    override fun isEligible(entry: ReferenceEntry) = true

    override fun calculate(entries: List<ReferenceEntry>, eligiblePopulationSize: Int): Measurement {
        return Measurement(entries.size.toDouble() / eligiblePopulationSize, entries.size)
    }

    override fun createTableElement(measurement: Measurement): TableElement {
        return TableElement.regular(percentage(measurement.value))
    }

    override fun title(): String {
        return "Treatment decisions (percentage of population assigned to systemic treatment) in NCR real-world data set"
    }
}
