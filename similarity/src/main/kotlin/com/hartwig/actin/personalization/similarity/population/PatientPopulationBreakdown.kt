package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.Diagnosis
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.TreatmentGroup
import org.jetbrains.kotlinx.kandy.ir.Plot

typealias DiagnosisAndEpisode = Pair<Diagnosis, Episode>

class PatientPopulationBreakdown(
    private val patientsByTreatment: List<Pair<TreatmentGroup, List<DiagnosisAndEpisode>>>,
    private val populationDefinitions: List<PopulationDefinition>,
    private val measurementTypes: List<MeasurementType> = MeasurementType.entries
) {
    fun analyze(): PersonalizedDataAnalysis {
        val allPatients = patientsByTreatment.flatMap { it.second }
        val populations = populationDefinitions.map { populationFromDefinition(it, allPatients) }
        val populationsByNameAndMeasurement = populations.associateBy(Population::name)

        val treatmentAnalyses = patientsByTreatment.map { (treatment, patientsWithTreatment) ->
            treatmentAnalysisForPatients(treatment, patientsWithTreatment, populationsByNameAndMeasurement)
        }

        return PersonalizedDataAnalysis(treatmentAnalyses, populations, plotsForPatients(allPatients))
    }

    private fun populationFromDefinition(
        populationDefinition: PopulationDefinition, allPatients: List<DiagnosisAndEpisode>
    ): Population {
        val matchingPatients = allPatients.filter(populationDefinition.criteria)
        val patientsByMeasurementType = measurementTypes.associateWith { measurementType ->
            matchingPatients.filter(measurementType.calculation::isEligible)
        }
        return Population(populationDefinition.name, patientsByMeasurementType)
    }

    private fun treatmentAnalysisForPatients(
        treatment: TreatmentGroup, patients: List<DiagnosisAndEpisode>, populationsByName: Map<String, Population>
    ): TreatmentAnalysis {
        val treatmentMeasurements = measurementTypes.associateWith { measurementType ->
            val patientsWithTreatmentAndMeasurement = patients.filter(measurementType.calculation::isEligible)
            populationDefinitions.associate { (title, criteria) ->
                val matchingPatients = patientsWithTreatmentAndMeasurement.filter(criteria)
                val eligiblePopulationSize = populationsByName[title]!!.patientsByMeasurementType[measurementType]!!.size
                title to measurementType.calculation.calculate(matchingPatients, eligiblePopulationSize)
            }
        }
        return TreatmentAnalysis(treatment, treatmentMeasurements)
    }

    private fun patientHasPfsMetrics(patient: DiagnosisAndEpisode): Boolean {
        val plan = patient.second.systemicTreatmentPlan
        return plan?.observedPfsDays != null && plan.hadProgressionEvent != null
    }

    private fun patientObservedPfsDays(patient: DiagnosisAndEpisode) =
        patient.second.systemicTreatmentPlan?.observedPfsDays

    private fun plotsForPatients(allPatients: List<DiagnosisAndEpisode>): Map<String, Plot> {
        val sortedPopulationsByName = populationDefinitions.associate { definition ->
            val patients = allPatients.filter(definition.criteria)
                .filter(::patientHasPfsMetrics)
                .sortedBy(::patientObservedPfsDays)
            definition.name to patients
        }
        val sortedPopulationsByTreatment = patientsByTreatment.associate { (treatment, patients) ->
            treatment.display to patients.filter(::patientHasPfsMetrics).sortedBy(::patientObservedPfsDays)
        }
        return listOfNotNull(
            PfsPlot.createPfsPlot(sortedPopulationsByName)?.let { "populations" to it },
            PfsPlot.createPfsPlot(sortedPopulationsByTreatment)?.let { "treatments" to it }
        )
            .toMap()
    }
}