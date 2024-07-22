package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.LocationGroup

const val ALL_PATIENTS_POPULATION_NAME = "All"

data class PopulationDefinition(val name: String, val criteria: (DiagnosisAndEpisode) -> Boolean) {

    companion object {
        fun createAllForPatientProfile(
            age: Int, whoStatus: Int, hasRasMutation: Boolean, metastasisLocationGroups: Set<LocationGroup>
        ): List<PopulationDefinition> {
            val minAge = age - 5
            val maxAge = age + 5

            return listOf(
                PopulationDefinition(ALL_PATIENTS_POPULATION_NAME) { true },
                PopulationDefinition("Age $minAge-${maxAge}y") { it.first.ageAtDiagnosis in minAge..maxAge },
                PopulationDefinition("WHO $whoStatus") { it.second.whoStatusPreTreatmentStart == whoStatus },
                PopulationDefinition(
                    "RAS ${if (hasRasMutation) "positive" else "negative"}"
                ) { (diagnosis, _) -> diagnosis.hasRasMutation == hasRasMutation },
                PopulationDefinition("${formatLocationGroups(metastasisLocationGroups)} lesions") { (_, episode) ->
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
