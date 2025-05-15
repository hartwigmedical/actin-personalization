package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.ReferenceEntry
import com.hartwig.actin.personalization.datamodel.diagnosis.LocationGroup
import com.hartwig.actin.personalization.datamodel.serialization.ReferenceEntryJson
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentGroup
import com.hartwig.actin.personalization.interpretation.TreatmentInterpreter
import com.hartwig.actin.personalization.similarity.population.PatientPopulationBreakdown
import com.hartwig.actin.personalization.similarity.population.PersonalizedDataAnalysis
import com.hartwig.actin.personalization.similarity.population.PopulationDefinition
import io.github.oshai.kotlinlogging.KotlinLogging

class PersonalizedDataInterpreter(val entriesByTreatment: List<Pair<TreatmentGroup, List<ReferenceEntry>>>) {

    fun analyzePatient(
        age: Int,
        whoStatus: Int,
        hasRasMutation: Boolean,
        metastasisLocationGroups: Set<LocationGroup>
    ): PersonalizedDataAnalysis {
        val populationDefinitions =
            PopulationDefinition.createAllForPatientProfile(age, whoStatus, hasRasMutation, metastasisLocationGroups)

        return PatientPopulationBreakdown(entriesByTreatment, populationDefinitions).analyze()
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}

        fun createFromFile(path: String): PersonalizedDataInterpreter {
            LOGGER.info { "Loading reference entry database from $path" }
            val referenceEntries = ReferenceEntryJson.read(path)

            LOGGER.info { " Loaded ${referenceEntries.size} reference entries" }
            return createFromReferenceEntries(referenceEntries)
        }

        fun createFromReferenceEntries(entries: List<ReferenceEntry>): PersonalizedDataInterpreter {
            val applicableEntries = entries
                .filter { TreatmentInterpreter(it.treatmentEpisodes).hasMetastaticTreatmentWithSystemicTreatmentOnly() }

            val entriesByTreatment = applicableEntries.groupBy { entry ->
                TreatmentInterpreter(entry.treatmentEpisodes).firstMetastaticSystemicTreatmentGroup()!!
            }
                .toList()
                .sortedByDescending { it.second.size }

            return PersonalizedDataInterpreter(entriesByTreatment)
        }
    }
}