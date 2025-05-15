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
                .filter { hasMetastaticTreatmentEpisodeWithSystemicTreatmentOnly(it) }

            val entriesByTreatment = applicableEntries.groupBy { entry ->
                val interpreter = TreatmentInterpreter(entry.treatmentEpisodes)
                interpreter.firstSpecificMetastaticSystemicTreatment()!!.treatment.treatmentGroup
            }
                .toList()
                .sortedByDescending { it.second.size }

            return PersonalizedDataInterpreter(entriesByTreatment)
        }

        private fun hasMetastaticTreatmentEpisodeWithSystemicTreatmentOnly(entry: ReferenceEntry): Boolean {
            // TODO (KD): Review whether filtering remains consistent with data frame used by notebooks eventually.
            val interpreter = TreatmentInterpreter(entry.treatmentEpisodes)
            val metastaticTreatmentEpisode = interpreter.extractMetastaticTreatmentEpisode() ?: return false

            return with(metastaticTreatmentEpisode) {
                interpreter.extractFirstSpecificSystemicTreatment(metastaticTreatmentEpisode) != null &&
                        gastroenterologyResections.isEmpty() &&
                        primarySurgeries.isEmpty() &&
                        metastaticSurgeries.isEmpty() &&
                        hipecTreatments.isEmpty() &&
                        primaryRadiotherapies.isEmpty() &&
                        metastaticRadiotherapies.isEmpty()
            }
        }
    }
}