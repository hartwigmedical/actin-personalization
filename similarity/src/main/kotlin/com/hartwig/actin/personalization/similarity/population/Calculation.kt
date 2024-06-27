package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.similarity.report.TableElement

interface Calculation {

    fun isEligible(patient: DiagnosisAndEpisode): Boolean

    fun calculate(patients: List<DiagnosisAndEpisode>, eligibleSubPopulationSize: Int): Measurement

    fun createTableElement(measurement: Measurement): TableElement

    fun title(): String
}