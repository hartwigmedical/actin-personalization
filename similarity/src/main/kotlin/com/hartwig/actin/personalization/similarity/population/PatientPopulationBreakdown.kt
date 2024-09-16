package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.Diagnosis
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.Treatment
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
        val filteredPatients = allPatients.filter(::patientHasPfsMetrics).sortedBy(::patientObservedPfsDays)
        val sortedPatientsByPopulation = populationDefinitions.associate { definition ->
            definition.name to filteredPatients.filter(definition.criteria)
        }

        val folfoxiriBPatients = filteredPatients.filter {
            it.second.systemicTreatmentPlan!!.treatment.treatmentGroup == TreatmentGroup.FOLFOXIRI_B
        }
        val folfoxBPatients = filteredPatients.filter {
            it.second.systemicTreatmentPlan!!.treatment == Treatment.FOLFOX_B
        }
        val folfoxBOrCapoxBPatients = filteredPatients.filter {
            it.second.systemicTreatmentPlan!!.treatment.treatmentGroup == TreatmentGroup.CAPOX_B_OR_FOLFOX_B
        }
        val simplePlots = listOfNotNull(
            PfsPlot.createPfsPlot(sortedPatientsByPopulation)?.let { "by population" to it },
            plotByWho(filteredPatients)?.let { "by WHO" to it },
            plotByWho(folfoxiriBPatients)?.let { "with FOLFOXIRI-B by WHO" to it },
            plotByWho(folfoxBPatients)?.let { "with FOLFOX-B by WHO" to it },
            plotByWho(folfoxBOrCapoxBPatients)?.let { "with CAPOX-B or FOLFOX-B by WHO" to it },
        )

        val populationPlotsByTreatment = populationDefinitions.mapNotNull { definition ->
            val filteredPatientsByTreatment = filteredPatients.filter(definition.criteria).groupBy { (_, episode) ->
                episode.systemicTreatmentPlan!!.treatment.treatmentGroup.display
            }
            PfsPlot.createPfsPlot(filteredPatientsByTreatment)
                ?.let { "for group ${definition.name} by treatment" to it }
        }

        return (simplePlots + populationPlotsByTreatment).toMap()
    }

    private fun plotByWho(sortedPatients: List<DiagnosisAndEpisode>): Plot? {
        val patientsByWho = sortedPatients.groupBy { (_, episode) -> "WHO ${episode.whoStatusPreTreatmentStart}" }
                .filter { (key, _) -> key != "WHO null" }
                .toSortedMap()
        return PfsPlot.createPfsPlot(patientsByWho)
    }
}