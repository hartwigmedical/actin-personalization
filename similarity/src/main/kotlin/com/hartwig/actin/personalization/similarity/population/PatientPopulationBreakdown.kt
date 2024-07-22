package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.Diagnosis
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.TreatmentGroup

typealias DiagnosisAndEpisode = Pair<Diagnosis, Episode>

class PatientPopulationBreakdown(
    private val patientsByTreatment: List<Pair<TreatmentGroup, List<DiagnosisAndEpisode>>>,
    private val populationDefinitions: List<PopulationDefinition>
) {
    fun analyze(): PersonalizedDataAnalysis {
        val allPatients = patientsByTreatment.flatMap { it.second }
        val populations = populationDefinitions.map { populationFromDefinition(it, allPatients) }
        val populationsByName = populations.associateBy(Population::name)

        val treatmentAnalyses = patientsByTreatment.map { (treatment, patientsWithTreatment) ->
            treatmentAnalysisForPatients(treatment, patientsWithTreatment, populationsByName)
        }

        return PersonalizedDataAnalysis(treatmentAnalyses, populations)
    }

    private fun populationFromDefinition(
        populationDefinition: PopulationDefinition, allPatients: List<DiagnosisAndEpisode>
    ): Population {
        val matchingPatients = allPatients.filter(populationDefinition.criteria)
        val patientsByMeasurementType = MeasurementType.entries.associateWith { measurementType ->
            matchingPatients.filter(measurementType.calculation::isEligible)
        }
        return Population(populationDefinition.name, patientsByMeasurementType)
    }

    private fun treatmentAnalysisForPatients(
        treatment: TreatmentGroup, patients: List<DiagnosisAndEpisode>, populationsByName: Map<String, Population>
    ): TreatmentAnalysis {
        val treatmentMeasurements = MeasurementType.entries.associateWith { measurementType ->
            val patientsWithTreatmentAndMeasurement = patients.filter(measurementType.calculation::isEligible)
            populationDefinitions.map { (title, criteria) ->
                val matchingPatients = patientsWithTreatmentAndMeasurement.filter(criteria)
                val eligiblePopulationSize = populationsByName[title]!!.patientsByMeasurementType[measurementType]!!.size
                title to measurementType.calculation.calculate(matchingPatients, eligiblePopulationSize)
            }.toMap()
        }
        return TreatmentAnalysis(treatment, treatmentMeasurements)
    }
}