package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.Diagnosis
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.TreatmentGroup

typealias DiagnosisAndEpisode = Pair<Diagnosis, Episode>

class PatientPopulationBreakdown(
    private val patientsByTreatment: List<Pair<TreatmentGroup, List<DiagnosisAndEpisode>>>,
    private val subPopulationDefinitions: List<SubPopulationDefinition>
) {
    fun analyze(): PersonalizedDataAnalysis {
        val allPatients = patientsByTreatment.flatMap { it.second }
        val subPopulations = subPopulationDefinitions.map { subPopulationFromDefinition(it, allPatients) }
        val subPopulationsByName = subPopulations.associateBy(SubPopulation::name)

        val treatmentAnalyses = patientsByTreatment.map { (treatment, patientsWithTreatment) ->
            treatmentAnalysisForPatients(treatment, patientsWithTreatment, subPopulationsByName)
        }

        return PersonalizedDataAnalysis(treatmentAnalyses, subPopulations)
    }

    private fun subPopulationFromDefinition(
        subPopulationDefinition: SubPopulationDefinition, allPatients: List<DiagnosisAndEpisode>
    ): SubPopulation {
        val matchingPatients = allPatients.filter(subPopulationDefinition.criteria)
        val patientsByMeasurementType = MeasurementType.entries.associateWith { measurementType ->
            matchingPatients.filter(measurementType.calculation::isEligible)
        }
        return SubPopulation(subPopulationDefinition.name, patientsByMeasurementType)
    }

    private fun treatmentAnalysisForPatients(
        treatment: TreatmentGroup, patients: List<DiagnosisAndEpisode>, subPopulationsByName: Map<String, SubPopulation>
    ): TreatmentAnalysis {
        val treatmentMeasurements = MeasurementType.entries.associateWith { measurementType ->
            val patientsWithTreatmentAndMeasurement = patients.filter(measurementType.calculation::isEligible)
            subPopulationDefinitions.map { (title, criteria) ->
                val matchingPatients = patientsWithTreatmentAndMeasurement.filter(criteria)
                val eligibleSubPopulationSize = subPopulationsByName[title]!!.patientsByMeasurementType[measurementType]!!.size
                title to measurementType.calculation.calculate(matchingPatients, eligibleSubPopulationSize)
            }.toMap()
        }
        return TreatmentAnalysis(treatment, treatmentMeasurements)
    }
}