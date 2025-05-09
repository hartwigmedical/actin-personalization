package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.ReferencePatient
import com.hartwig.actin.personalization.datamodel.Tumor
import com.hartwig.actin.personalization.datamodel.diagnosis.LocationGroup
import com.hartwig.actin.personalization.datamodel.serialization.ReferencePatientJson
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentGroup
import com.hartwig.actin.personalization.similarity.population.PatientPopulationBreakdown
import com.hartwig.actin.personalization.similarity.population.PersonalizedDataAnalysis
import com.hartwig.actin.personalization.similarity.population.PopulationDefinition
import com.hartwig.actin.personalization.similarity.selection.TreatmentSelection
import io.github.oshai.kotlinlogging.KotlinLogging

class PersonalizedDataInterpreter(val entriesByTreatment: List<Pair<TreatmentGroup, List<Tumor>>>) {

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
            LOGGER.info { "Loading reference patient database from $path" }
            val patients = ReferencePatientJson.read(path)

            LOGGER.info { " Loaded ${patients.size} reference patients" }
            return createFromReferencePatients(patients)
        }

        fun createFromReferencePatients(patients: List<ReferencePatient>): PersonalizedDataInterpreter {
            val referenceEntries = patients
                .flatMap(ReferencePatient::tumors)
                .filter { hasMetastaticTreatmentEpisodeWithSystemicTreatmentOnly(it) }

            val entriesByTreatment = referenceEntries.groupBy { entry ->
                TreatmentSelection.firstSpecificMetastaticSystemicTreatment(entry)!!.treatment.treatmentGroup
            }
                .toList()
                .sortedByDescending { it.second.size }

            return PersonalizedDataInterpreter(entriesByTreatment)
        }

        private fun hasMetastaticTreatmentEpisodeWithSystemicTreatmentOnly(entry: Tumor): Boolean {
            // TODO (KD): Review whether filtering remains consistent with data frame used by notebooks eventually.
            val metastaticTreatmentEpisode = TreatmentSelection.extractMetastaticTreatmentEpisode(entry) ?: return false

            return with(metastaticTreatmentEpisode) {
                TreatmentSelection.extractFirstSpecificSystemicTreatment(metastaticTreatmentEpisode) != null &&
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