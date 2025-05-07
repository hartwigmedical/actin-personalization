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

class PersonalizedDataInterpreter(val tumorsByTreatment: List<Pair<TreatmentGroup, List<Tumor>>>) {

    fun analyzePatient(
        age: Int,
        whoStatus: Int,
        hasRasMutation: Boolean,
        metastasisLocationGroups: Set<LocationGroup>
    ): PersonalizedDataAnalysis {
        val populationDefinitions =
            PopulationDefinition.createAllForPatientProfile(age, whoStatus, hasRasMutation, metastasisLocationGroups)

        return PatientPopulationBreakdown(tumorsByTreatment, populationDefinitions).analyze()
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
            val referenceTumors = patients
                .flatMap(ReferencePatient::tumors)
                .filter { hasMetastaticTreatmentEpisodeWithSystemicTreatmentOnly(it) }

            val tumorsByTreatment = referenceTumors.groupBy { tumor ->
                TreatmentSelection.firstSpecificMetastaticSystemicTreatment(tumor)!!.treatment.treatmentGroup
            }
                .toList()
                .sortedByDescending { it.second.size }

            return PersonalizedDataInterpreter(tumorsByTreatment)
        }

        private fun hasMetastaticTreatmentEpisodeWithSystemicTreatmentOnly(tumor: Tumor): Boolean {
            // TODO (KD): Review whether filtering remains consistent with view in SQL.
            val metastaticTreatmentEpisode = TreatmentSelection.extractMetastaticTreatmentEpisode(tumor) ?: return false

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