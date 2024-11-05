package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.DiagnosisEpisodeTreatment
import com.hartwig.actin.personalization.similarity.report.TableElement

interface Calculation {

    fun isEligible(patient: DiagnosisEpisodeTreatment): Boolean

    fun calculate(patients: List<DiagnosisEpisodeTreatment>, eligiblePopulationSize: Int): Measurement

    fun createTableElement(measurement: Measurement): TableElement

    fun title(): String
}