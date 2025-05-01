package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.Tumor
import com.hartwig.actin.personalization.similarity.report.TableElement
import com.hartwig.actin.personalization.similarity.report.percentage

object TreatmentDecisionCalculation : Calculation {

    override fun isEligible(tumor: Tumor) = true

    override fun calculate(tumors: List<Tumor>, eligiblePopulationSize: Int): Measurement {
        return Measurement(tumors.size.toDouble() / eligiblePopulationSize, tumors.size)
    }

    override fun createTableElement(measurement: Measurement): TableElement {
        return TableElement.regular(percentage(measurement.value))
    }

    override fun title(): String {
        return "Treatment decisions (percentage of population assigned to systemic treatment) in NCR real-world data set"
    }
}
