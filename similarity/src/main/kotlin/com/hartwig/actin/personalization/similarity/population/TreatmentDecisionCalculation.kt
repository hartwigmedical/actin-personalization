package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.DiagnosisEpisodeTreatment
import com.hartwig.actin.personalization.similarity.report.TableElement
import com.hartwig.actin.personalization.similarity.report.percentage

object TreatmentDecisionCalculation : Calculation {

    override fun isEligible(patient: DiagnosisEpisodeTreatment) = true

    override fun calculate(patients: List<DiagnosisEpisodeTreatment>, eligiblePopulationSize: Int): Measurement {
        return Measurement(patients.size.toDouble() / eligiblePopulationSize, patients.size)
    }

    override fun createTableElement(measurement: Measurement): TableElement {
        return TableElement.regular(percentage(measurement.value))
    }

    override fun title(): String {
        return "Treatment decisions (percentage of population assigned to systemic treatment) in NCR real-world data set"
    }
}
