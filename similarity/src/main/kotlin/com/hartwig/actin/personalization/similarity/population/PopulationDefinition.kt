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
                PopulationDefinition("WHO $whoStatus") { tumor -> tumor.whoAssessments.any { it.whoStatus == whoStatus } },
                PopulationDefinition(
                    "RAS ${if (hasRasMutation) "positive" else "negative"}"
                ) { tumor -> tumor.molecularResults.any { it.hasRasMutation == hasRasMutation } },
                PopulationDefinition("${formatLocationGroups(metastasisLocationGroups)} lesions") { tumor ->
                    tumorMatchesMetastasisLocationGroups(tumor, metastasisLocationGroups)
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

        private fun tumorMatchesMetastasisLocationGroups(tumor: Tumor, metastasisLocationGroups: Set<LocationGroup>): Boolean {
            val cutoffDays =
                TreatmentSelection.firstSpecificMetastaticSystemicTreatment(tumor)?.daysBetweenDiagnosisAndStart ?: Int.MAX_VALUE

            val groups = tumor.metastaticDiagnosis.metastases.filter { metastasis ->
                metastasis.daysSinceDiagnosis?.let { it < cutoffDays } == true
            }
                .map { it.location.locationGroup.topLevelGroup() }
                .toSet()

            return groups == metastasisLocationGroups
        }
    }
}
