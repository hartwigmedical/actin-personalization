package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.similarity.report.TableElement
import java.util.Locale

object TreatmentDecisionCalculation : Calculation {

    override fun isEligible(patient: DiagnosisAndEpisode) = true

    override fun calculate(patients: List<DiagnosisAndEpisode>, eligiblePopulationSize: Int): Measurement {
        return Measurement(patients.size.toDouble() / eligiblePopulationSize, patients.size)
    }

    override fun createTableElement(measurement: Measurement): TableElement {
        return TableElement.regular(String.format(Locale.ENGLISH, "%.1f%%", 100.0 * measurement.value))
    }

    override fun title(): String {
        return "Treatment decisions (percentage of population assigned to systemic treatment) in NCR real-world data set"
    }
}
