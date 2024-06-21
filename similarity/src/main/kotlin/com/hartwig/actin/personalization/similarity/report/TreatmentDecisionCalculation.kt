package com.hartwig.actin.personalization.similarity.report

import com.hartwig.actin.personalization.similarity.population.Calculation
import com.hartwig.actin.personalization.similarity.population.DiagnosisAndEpisode
import com.hartwig.actin.personalization.similarity.population.Measurement

object TreatmentDecisionCalculation : Calculation {
    override fun isEligible(patient: DiagnosisAndEpisode) = true

    override fun calculate(patients: List<DiagnosisAndEpisode>, eligibleSubPopulationSize: Int): Measurement {
        return Measurement(patients.size.toDouble() / eligibleSubPopulationSize, patients.size)
    }

    override fun createTableElement(measurement: Measurement): TableElement {
        return TableElement.regular(String.format("%.1f%%", 100.0 * measurement.value))
    }

    override fun title(): String {
        return "Treatment decisions (percentage of population assigned to systemic treatment) in NCR real-world data set"
    }
}
