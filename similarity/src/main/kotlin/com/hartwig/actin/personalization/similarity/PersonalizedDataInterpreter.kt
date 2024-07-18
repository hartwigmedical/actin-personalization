package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.DistantMetastasesStatus
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.LocationGroup
import com.hartwig.actin.personalization.datamodel.PatientRecord
import com.hartwig.actin.personalization.datamodel.Treatment
import com.hartwig.actin.personalization.datamodel.TreatmentGroup
import com.hartwig.actin.personalization.datamodel.serialization.PatientRecordJson
import com.hartwig.actin.personalization.similarity.population.DiagnosisAndEpisode
import com.hartwig.actin.personalization.similarity.population.PatientPopulationBreakdown
import com.hartwig.actin.personalization.similarity.population.PersonalizedDataAnalysis
import com.hartwig.actin.personalization.similarity.population.PopulationDefinition
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private fun Episode.doesNotIncludeAdjuvantOrNeoadjuvantTreatment(): Boolean {
    return !hasHadPreSurgerySystemicChemotherapy &&
            !hasHadPostSurgerySystemicChemotherapy &&
            !hasHadPreSurgerySystemicTargetedTherapy &&
            !hasHadPostSurgerySystemicTargetedTherapy
}

class PersonalizedDataInterpreter(val patientsByTreatment: List<Pair<TreatmentGroup, List<DiagnosisAndEpisode>>>) {

    fun analyzePatient(
        age: Int, whoStatus: Int, hasRasMutation: Boolean, metastasisLocationGroups: Set<LocationGroup>
    ): PersonalizedDataAnalysis {
        val populationDefinitions =
            PopulationDefinition.createAllForPatientProfile(age, whoStatus, hasRasMutation, metastasisLocationGroups)

        return PatientPopulationBreakdown(patientsByTreatment, populationDefinitions).analyze()
    }

    companion object {
        private val LOGGER: Logger = LogManager.getLogger(PersonalizedDataInterpreter::class.java)

        fun createFromFile(path: String): PersonalizedDataInterpreter {
            LOGGER.info("Loading patient records from file $path")
            val patients = PatientRecordJson.read(path)
            LOGGER.info(" Loaded {} patient records", patients.size)
            return createFromPatientRecords(patients)
        }

        fun createFromPatientRecords(patients: List<PatientRecord>): PersonalizedDataInterpreter {
            val referencePop = patients.flatMap(PatientRecord::tumorEntries).map { (diagnosis, episodes) ->
                diagnosis to episodes.single { it.order == 1 }
            }
                .filter { (_, episode) ->
                    episode.distantMetastasesStatus == DistantMetastasesStatus.AT_START &&
                            episode.systemicTreatmentPlan?.treatment?.let { it != Treatment.OTHER } == true &&
                            episode.surgeries.isEmpty() &&
                            episode.doesNotIncludeAdjuvantOrNeoadjuvantTreatment()
                }

            val patientsByTreatment = referencePop.groupBy { (_, episode) ->
                episode.systemicTreatmentPlan!!.treatment.treatmentGroup
            }
                .toList()
                .sortedByDescending { it.second.size }

            return PersonalizedDataInterpreter(patientsByTreatment)
        }
    }
}