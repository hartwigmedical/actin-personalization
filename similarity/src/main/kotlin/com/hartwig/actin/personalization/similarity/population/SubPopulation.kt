package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.TreatmentGroup

data class SubPopulation(
    val name: String,
    val patients: List<DiagnosisAndEpisode>,
    val patientsByTreatment: List<Pair<TreatmentGroup, List<DiagnosisAndEpisode>>>
)
