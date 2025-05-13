package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.ReferenceEntry
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentGroup
import com.hartwig.actin.personalization.similarity.selection.TreatmentSelection
import org.jetbrains.kotlinx.kandy.ir.Plot

class PatientPopulationBreakdown(
    private val entriesByTreatment: List<Pair<TreatmentGroup, List<ReferenceEntry>>>,
    private val populationDefinitions: List<PopulationDefinition>,
    private val measurementTypes: List<MeasurementType> = MeasurementType.entries
) {
    
    fun analyze(): PersonalizedDataAnalysis {
        val allEntries = entriesByTreatment.flatMap { it.second }
        val populations = populationDefinitions.map { populationFromDefinition(it, allEntries) }
        val populationsByNameAndMeasurement = populations.associateBy(Population::name)

        val treatmentAnalyses = entriesByTreatment.map { (treatment, entriesWithTreatment) ->
            treatmentAnalysisForEntries(treatment, entriesWithTreatment, populationsByNameAndMeasurement)
        }

        return PersonalizedDataAnalysis(treatmentAnalyses, populations, plotsForEntries(allEntries))
    }

    private fun populationFromDefinition(populationDefinition: PopulationDefinition, allEntries: List<ReferenceEntry>): Population {
        val matchingEntries = allEntries.filter(populationDefinition.criteria)
        val entriesByMeasurementType = measurementTypes.associateWith { measurementType ->
            matchingEntries.filter(measurementType.calculation::isEligible)
        }
        return Population(populationDefinition.name, entriesByMeasurementType)
    }

    private fun treatmentAnalysisForEntries(
        treatment: TreatmentGroup, entries: List<ReferenceEntry>, populationsByName: Map<String, Population>
    ): TreatmentAnalysis {
        val treatmentMeasurements = measurementTypes.associateWith { measurementType ->
            val entriesWithTreatmentAndMeasurement = entries.filter(measurementType.calculation::isEligible)
            populationDefinitions.associate { (name, criteria) ->
                val matchingEntries = entriesWithTreatmentAndMeasurement.filter(criteria)
                val eligiblePopulationSize = populationsByName[name]!!.entriesByMeasurementType[measurementType]!!.size
                name to measurementType.calculation.calculate(matchingEntries, eligiblePopulationSize)
            }
        }
        return TreatmentAnalysis(treatment, treatmentMeasurements)
    }

    private fun createPlotsForMeasurement(
        allEntries: List<ReferenceEntry>, calculation: SurvivalCalculation, yAxisLabel: String
    ): Map<String, Plot> {
        val filteredEntries = allEntries.filter(calculation::isEligible).sortedBy {
            calculation.timeFunction(it)!!
        }
        val groupedEntriesByPopulation = populationDefinitions.associate { definition ->
            definition.name to filteredEntries.filter { definition.criteria(it) }
        }
        val plots = listOfNotNull(
            SurvivalPlot.createSurvivalPlot(groupedEntriesByPopulation, calculation, yAxisLabel)?.let {
                "$yAxisLabel by population" to it
            },
            SurvivalPlot.createSurvivalPlot(groupByWho(filteredEntries), calculation, yAxisLabel)?.let {
                "$yAxisLabel by WHO" to it
            }
        )

        val populationPlotsByTreatment = populationDefinitions.mapNotNull { definition ->
            val entriesByTreatment = filteredEntries.filter { definition.criteria(it) }.groupBy {
                TreatmentSelection.firstSpecificMetastaticSystemicTreatment(it)!!.treatment.treatmentGroup.display
            }
            SurvivalPlot.createSurvivalPlot(entriesByTreatment, calculation, yAxisLabel)
                ?.let { "$yAxisLabel for group ${definition.name} by treatment" to it }
        }

        return (plots + populationPlotsByTreatment).toMap()
    }

    private fun plotsForEntries(allEntries: List<ReferenceEntry>): Map<String, Plot> {
        val pfsPlots = createPlotsForMeasurement(allEntries, PFS_CALCULATION, yAxisLabel = "PFS %")
        val osPlots = createPlotsForMeasurement(allEntries, OS_CALCULATION, yAxisLabel = "OS %")
        return pfsPlots + osPlots
    }

    private fun groupByWho(entries: List<ReferenceEntry>) =
        entries.groupBy { "WHO ${it.whoAssessments.firstOrNull()?.whoStatus}" }.filterKeys { it != "WHO null" }
}
