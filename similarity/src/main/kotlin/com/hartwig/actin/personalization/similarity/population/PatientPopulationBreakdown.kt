package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.Tumor
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentGroup
import com.hartwig.actin.personalization.similarity.selection.TreatmentSelection
import org.jetbrains.kotlinx.kandy.ir.Plot

class PatientPopulationBreakdown(
    private val tumorsByTreatment: List<Pair<TreatmentGroup, List<Tumor>>>,
    private val populationDefinitions: List<PopulationDefinition>,
    private val measurementTypes: List<MeasurementType> = MeasurementType.entries
) {
    fun analyze(): PersonalizedDataAnalysis {
        val allTumors = tumorsByTreatment.flatMap { it.second }
        val populations = populationDefinitions.map { populationFromDefinition(it, allTumors) }
        val populationsByNameAndMeasurement = populations.associateBy(Population::name)

        val treatmentAnalyses = tumorsByTreatment.map { (treatment, tumorsWithTreatment) ->
            treatmentAnalysisForTumors(treatment, tumorsWithTreatment, populationsByNameAndMeasurement)
        }

        return PersonalizedDataAnalysis(treatmentAnalyses, populations, plotsForTumors(allTumors))
    }

    private fun populationFromDefinition(populationDefinition: PopulationDefinition, allTumors: List<Tumor>): Population {
        val matchingTumors = allTumors.filter(populationDefinition.criteria)
        val tumorsByMeasurementType = measurementTypes.associateWith { measurementType ->
            matchingTumors.filter(measurementType.calculation::isEligible)
        }
        return Population(populationDefinition.name, tumorsByMeasurementType)
    }

    private fun treatmentAnalysisForTumors(
        treatment: TreatmentGroup, tumors: List<Tumor>, populationsByName: Map<String, Population>
    ): TreatmentAnalysis {
        val treatmentMeasurements = measurementTypes.associateWith { measurementType ->
            val patientsWithTreatmentAndMeasurement = tumors.filter(measurementType.calculation::isEligible)
            populationDefinitions.associate { (name, criteria) ->
                val matchingPatients = patientsWithTreatmentAndMeasurement.filter(criteria)
                val eligiblePopulationSize = populationsByName[name]!!.tumorsByMeasurementType[measurementType]!!.size
                name to measurementType.calculation.calculate(matchingPatients, eligiblePopulationSize)
            }
        }
        return TreatmentAnalysis(treatment, treatmentMeasurements)
    }

    private fun createPlotsForMeasurement(
        allTumors: List<Tumor>, calculation: SurvivalCalculation, yAxisLabel: String
    ): Map<String, Plot> {
        val filteredTumors = allTumors.filter(calculation::isEligible).sortedBy {
            calculation.timeFunction(it)!!
        }
        val groupedPatientsByPopulation = populationDefinitions.associate { definition ->
            definition.name to filteredTumors.filter { definition.criteria(it) }
        }
        val plots = listOfNotNull(
            SurvivalPlot.createSurvivalPlot(groupedPatientsByPopulation, calculation, yAxisLabel)?.let {
                "$yAxisLabel by population" to it
            },
            SurvivalPlot.createSurvivalPlot(groupByWho(filteredTumors), calculation, yAxisLabel)?.let {
                "$yAxisLabel by WHO" to it
            }
        )

        val populationPlotsByTreatment = populationDefinitions.mapNotNull { definition ->
            val patientsByTreatment = filteredTumors.filter { definition.criteria(it) }.groupBy {
                TreatmentSelection.definedMetastaticSystemicTreatment(it)!!.treatment.treatmentGroup.display
            }
            SurvivalPlot.createSurvivalPlot(patientsByTreatment, calculation, yAxisLabel)
                ?.let { "$yAxisLabel for group ${definition.name} by treatment" to it }
        }

        return (plots + populationPlotsByTreatment).toMap()
    }

    private fun plotsForTumors(allPatients: List<Tumor>): Map<String, Plot> {
        val pfsPlots = createPlotsForMeasurement(allPatients, PFS_CALCULATION, yAxisLabel = "PFS %")
        val osPlots = createPlotsForMeasurement(allPatients, OS_CALCULATION, yAxisLabel = "OS %")
        return pfsPlots + osPlots
    }

    private fun groupByWho(patients: List<Tumor>) =
        patients.groupBy { "WHO ${it.whoAssessments.firstOrNull()?.whoStatus}" }.filterKeys { it != "WHO null" }
}
