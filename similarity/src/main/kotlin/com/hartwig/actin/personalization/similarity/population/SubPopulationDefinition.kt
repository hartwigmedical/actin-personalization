package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.LocationGroup

const val ALL_PATIENTS_SUB_POPULATION_NAME = "All"

data class SubPopulationDefinition(val name: String, val criteria: (DiagnosisAndEpisode) -> Boolean) {

    companion object {
        fun createAllForPatientProfile(
            age: Int, whoStatus: Int, hasRasMutation: Boolean, metastasisLocationGroups: Set<LocationGroup>
        ): List<SubPopulationDefinition> {
            val minAge = age - 5
            val maxAge = age + 5

            return listOf(
                SubPopulationDefinition(ALL_PATIENTS_SUB_POPULATION_NAME) { true },
                SubPopulationDefinition("Age $minAge-${maxAge}y") { it.first.ageAtDiagnosis in minAge..maxAge },
                SubPopulationDefinition("WHO $whoStatus") { it.second.whoStatusPreTreatmentStart == whoStatus },
                SubPopulationDefinition(
                    "RAS ${if (hasRasMutation) "positive" else "negative"}"
                ) { (diagnosis, _) -> diagnosis.hasRasMutation == hasRasMutation },
                SubPopulationDefinition("${formatLocationGroups(metastasisLocationGroups)} lesions") { (_, episode) ->
                    episodeMatchesMetastasisLocationGroups(episode, metastasisLocationGroups)
                }
            )
        }

        private fun formatLocationGroups(locationGroups: Set<LocationGroup>): String {
            val locationNames = locationGroups.map(LocationGroup::display)
            return when (locationNames.size) {
                0 -> "No"
                1 -> "${locationNames.single()} only"
                else -> (locationNames.dropLast(2) + locationNames.takeLast(2).joinToString(" & ")).joinToString(separator = ", ")
            }
        }

        private fun episodeMatchesMetastasisLocationGroups(episode: Episode, metastasisLocationGroups: Set<LocationGroup>): Boolean =
            episode.systemicTreatmentPlan?.intervalTumorIncidenceTreatmentPlanStart?.let { planStart ->
                val groups = episode.metastases.filter { metastasis ->
                    metastasis.intervalTumorIncidenceMetastasisDetection?.let { it < planStart } == true
                }
                    .map { it.location.locationGroup.topLevelGroup() }
                    .toSet()
                groups == metastasisLocationGroups
            } == true
    }
}
