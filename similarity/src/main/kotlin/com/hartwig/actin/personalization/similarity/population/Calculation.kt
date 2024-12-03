package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.DiagnosisEpisode
import com.hartwig.actin.personalization.similarity.report.TableElement

interface Calculation {

    fun isEligible(patient: DiagnosisEpisode): Boolean

    fun calculate(patients: List<DiagnosisEpisode>, eligiblePopulationSize: Int): Measurement

    fun createTableElement(measurement: Measurement): TableElement

    fun title(): String
}