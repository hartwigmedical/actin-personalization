package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.DiagnosisEpisodeTreatment
import com.hartwig.actin.personalization.datamodel.TreatmentGroup
import org.jetbrains.kotlinx.kandy.ir.Plot

class PatientPopulationBreakdown(
    private val patientsByTreatment: List<Pair<TreatmentGroup, List<DiagnosisEpisodeTreatment>>>,
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
        populationDefinition: PopulationDefinition, allPatients: List<DiagnosisEpisodeTreatment>
    ): Population {
        val matchingPatients = allPatients.filter { populationDefinition.criteria(it) }
        val patientsByMeasurementType = measurementTypes.associateWith { measurementType ->
            matchingPatients.filter{measurementType.calculation.isEligible(it)}
        }
        return Population(populationDefinition.name, patientsByMeasurementType)
    }

    private fun treatmentAnalysisForPatients(
        treatment: TreatmentGroup, patients: List<DiagnosisEpisodeTreatment>, populationsByName: Map<String, Population>
    ): TreatmentAnalysis {
        val treatmentMeasurements = measurementTypes.associateWith { measurementType ->
            val patientsWithTreatmentAndMeasurement = patients.filter(measurementType.calculation::isEligible)
            populationDefinitions.associate { definition ->
                val title = definition.name
                val criteria = definition.criteria
                val matchingPatients = patientsWithTreatmentAndMeasurement.filter(criteria)
                val eligiblePopulationSize = populationsByName[title]!!.patientsByMeasurementType[measurementType]!!.size
                title to measurementType.calculation.calculate(matchingPatients, eligiblePopulationSize)
            }
        }
        return TreatmentAnalysis(treatment, treatmentMeasurements)
    }

    private fun createPlotsForMeasurement(
        allPatients: List<DiagnosisEpisodeTreatment>, calculation: SurvivalCalculation, yAxisLabel: String
    ): Map<String, Plot> {
        val filteredPatients = allPatients.filter(calculation::isEligible).sortedBy {
            calculation.timeFunction(it) ?: Int.MAX_VALUE
        }
        val groupedPatientsByPopulation = populationDefinitions.associate { definition ->
            definition.name to filteredPatients.filter { definition.criteria(it) }
        }
        val plots = listOfNotNull(
            SurvivalPlot.createSurvivalPlot(groupedPatientsByPopulation, calculation, yAxisLabel)?.let {
                "$yAxisLabel by population" to it
            },
            SurvivalPlot.createSurvivalPlot(groupByWho(filteredPatients), calculation, yAxisLabel)?.let {
                "WHO" to it
            }
        ).toMap()

        val populationPlotsByTreatment = populationDefinitions.mapNotNull { definition ->
            val patientsByTreatment = filteredPatients.filter { definition.criteria(it) }.groupBy {
                it.systemicTreatmentPlan?.treatment?.treatmentGroup?.display ?: "Unknown"
            }
            SurvivalPlot.createSurvivalPlot(patientsByTreatment, calculation, yAxisLabel)
                ?.let { "$yAxisLabel for group ${definition.name} by treatment" to it }
        }

        return (plots + populationPlotsByTreatment).toMap()
    }

    private fun plotsForPatients(allPatients: List<DiagnosisEpisodeTreatment>): Map<String, Plot> {
        val pfsPlots = createPlotsForMeasurement(allPatients, PFS_CALCULATION, yAxisLabel = "PFS %")
        val osPlots = createPlotsForMeasurement(allPatients, OS_CALCULATION, yAxisLabel = "OS %")
        return pfsPlots + osPlots
    }

    private fun groupByWho(patients: List<DiagnosisEpisodeTreatment>) =
        patients.groupBy { "WHO ${it.episode.whoStatusPreTreatmentStart}" }.filterKeys { it != "WHO null" }
}
