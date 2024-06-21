package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.Treatment

data class SubPopulation(
    val name: String,
    val patients: List<DiagnosisAndEpisode>,
    val patientsByTreatment: List<Pair<Treatment, List<DiagnosisAndEpisode>>>
)
