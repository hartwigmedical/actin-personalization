package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.Tumor
import com.hartwig.actin.personalization.datamodel.diagnosis.LocationGroup
import com.hartwig.actin.personalization.similarity.selection.TreatmentSelection

const val ALL_PATIENTS_POPULATION_NAME = "All"

data class PopulationDefinition(val name: String, val criteria: (Tumor) -> Boolean) {

    companion object {
        fun createAllForPatientProfile(
            age: Int, whoStatus: Int, hasRasMutation: Boolean, metastasisLocationGroups: Set<LocationGroup>
        ): List<PopulationDefinition> {
            val minAge = age - 5
            val maxAge = age + 5
            
            return listOf(
                PopulationDefinition(ALL_PATIENTS_POPULATION_NAME) { true },
                PopulationDefinition("Age $minAge-${maxAge}y") { it.ageAtDiagnosis in minAge..maxAge },
                PopulationDefinition("WHO $whoStatus") { entry -> entry.whoAssessments.any { it.whoStatus == whoStatus } },
                PopulationDefinition(
                    "RAS ${if (hasRasMutation) "positive" else "negative"}"
                ) { entry -> entry.molecularResults.any { it.hasRasMutation == hasRasMutation } },
                PopulationDefinition("${formatLocationGroups(metastasisLocationGroups)} lesions") { entry ->
                    entryMatchesMetastasisLocationGroups(entry, metastasisLocationGroups)
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

        private fun entryMatchesMetastasisLocationGroups(entry: Tumor, metastasisLocationGroups: Set<LocationGroup>): Boolean {
            val cutoffDays =
                TreatmentSelection.firstSpecificMetastaticSystemicTreatment(entry)?.daysBetweenDiagnosisAndStart ?: Int.MAX_VALUE

            val groups = entry.metastaticDiagnosis.metastases.filter { metastasis ->
                metastasis.daysSinceDiagnosis?.let { it < cutoffDays } == true
            }
                .map { it.location.locationGroup.topLevelGroup() }
                .toSet()

            return groups == metastasisLocationGroups
        }
    }
}
