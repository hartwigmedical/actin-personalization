package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.Diagnosis
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.SystemicTreatmentPlan
import com.hartwig.actin.personalization.datamodel.Treatment
import com.hartwig.actin.personalization.datamodel.TreatmentGroup
import org.jetbrains.kotlinx.kandy.ir.Plot


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

    private fun <T> createPlotsForMeasurement(
        allPatients: List<DiagnosisAndEpisode>, calculation: SurvivalCalculation<T>, yAxisLabel: String
    ): Map<String, Plot> {
        val filteredPatients = allPatients.filter(calculation::isEligible).sortedBy {
            calculation.extractor(it)?.let { item -> calculation.timeFunction(item) } ?: Int.MAX_VALUE
        }
        val groupedPatientsByPopulation = populationDefinitions.associate { definition ->
            definition.name to filteredPatients.filter(definition.criteria)
        }
        val plots = listOfNotNull(
            SurvivalPlot.createSurvivalPlot(groupedPatientsByPopulation, calculation, yAxisLabel)?.let {
                "$yAxisLabel by population" to it
            },
            ("WHO" to { p: List<DiagnosisAndEpisode> -> groupByWho(p) }).let { (label, filterFunction) ->
            filterFunction(filteredPatients).let { group ->
                SurvivalPlot.createSurvivalPlot(group, calculation, yAxisLabel)?.let {
                    "$yAxisLabel with $label by group" to it
                }
            }
        }
        ).toMap()

        val populationPlotsByTreatment = populationDefinitions.mapNotNull { definition ->
            val patientsByTreatment = filteredPatients.filter(definition.criteria).groupBy {
                it.second.systemicTreatmentPlan!!.treatment.treatmentGroup.display
            }
            SurvivalPlot.createSurvivalPlot(patientsByTreatment, calculation, yAxisLabel)
                ?.let { "$yAxisLabel for group ${definition.name} by treatment" to it }
        }

        return plots.toMap()
    }

    private fun plotsForPatients(allPatients: List<DiagnosisAndEpisode>): Map<String, Plot> {
        val pfsPlots = createPlotsForMeasurement(allPatients, PFS_CALCULATION, yAxisLabel = "PFS %")

        val osPlots = createPlotsForMeasurement(allPatients, OS_CALCULATION, yAxisLabel = "OS %")

        return pfsPlots + osPlots
    }

    private fun groupByWho(patients: List<DiagnosisAndEpisode>) =
        patients.groupBy { "WHO ${it.second.whoStatusPreTreatmentStart}" }.filterKeys { it != "WHO null" }

    private fun groupByTreatment(
        patients: List<DiagnosisAndEpisode>, treatment: Any
    ): Map<String, List<DiagnosisAndEpisode>>? {
        val groupedPatients = patients.filter {
            val plan = it.second.systemicTreatmentPlan
            when (treatment) {
                is TreatmentGroup -> plan?.treatment?.treatmentGroup == treatment
                is Treatment -> plan?.treatment == treatment
                else -> false
            }
        }
        return if (groupedPatients.isNotEmpty()) mapOf(treatment.toString() to groupedPatients) else null
    }
   }
