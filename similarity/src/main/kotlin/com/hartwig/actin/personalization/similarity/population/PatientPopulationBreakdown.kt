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

    private fun patientHasOsMetrics(patient: DiagnosisAndEpisode): Boolean {
        val plan = patient.second.systemicTreatmentPlan
        return plan?.observedOsFromTreatmentStartDays != null && plan.hadSurvivalEvent != null
    }

    private fun patientObservedPfsDays(patient: DiagnosisAndEpisode) =
        patient.second.systemicTreatmentPlan?.observedPfsDays

    private fun patientObservedOsDays(patient: DiagnosisAndEpisode) =
        patient.second.systemicTreatmentPlan?.observedOsFromTreatmentStartDays

    private fun plotsForPatients(allPatients: List<DiagnosisAndEpisode>): Map<String, Plot> {
        val filteredPatientsForPfs = allPatients.filter(::patientHasPfsMetrics).sortedBy(::patientObservedPfsDays)
        val filteredPatientsForOs = allPatients.filter(::patientHasOsMetrics).sortedBy(::patientObservedOsDays)

        val sortedPatientsByPopulationForPfs = populationDefinitions.associate { definition ->
            definition.name to filteredPatientsForPfs.filter(definition.criteria)
        }
        val sortedPatientsByPopulationForOs = populationDefinitions.associate { definition ->
            definition.name to filteredPatientsForOs.filter(definition.criteria)
        }

        val folfoxiriBPatientsForPfs = filteredPatientsForPfs.filter {
            it.second.systemicTreatmentPlan!!.treatment.treatmentGroup == TreatmentGroup.FOLFOXIRI_B
        }
        val folfoxiriBPatientsForOs = filteredPatientsForOs.filter {
            it.second.systemicTreatmentPlan!!.treatment.treatmentGroup == TreatmentGroup.FOLFOXIRI_B
        }

        val folfoxBPatientsForPfs = filteredPatientsForPfs.filter {
            it.second.systemicTreatmentPlan!!.treatment == Treatment.FOLFOX_B
        }
        val folfoxBPatientsForOs = filteredPatientsForOs.filter {
            it.second.systemicTreatmentPlan!!.treatment == Treatment.FOLFOX_B
        }

        val folfoxBOrCapoxBPatientsForPfs = filteredPatientsForPfs.filter {
            it.second.systemicTreatmentPlan!!.treatment.treatmentGroup == TreatmentGroup.CAPOX_B_OR_FOLFOX_B
        }
        val folfoxBOrCapoxBPatientsForOs = filteredPatientsForOs.filter {
            it.second.systemicTreatmentPlan!!.treatment.treatmentGroup == TreatmentGroup.CAPOX_B_OR_FOLFOX_B
        }

        val pfsPlots = listOfNotNull(
            PfsPlot.createPfsPlot(sortedPatientsByPopulationForPfs)?.let { "PFS by population" to it },
            plotByWho(filteredPatientsForPfs)?.let { "PFS by WHO" to it },
            plotByWho(folfoxiriBPatientsForPfs)?.let { "PFS with FOLFOXIRI-B by WHO" to it },
            plotByWho(folfoxBPatientsForPfs)?.let { "PFS with FOLFOX-B by WHO" to it },
            plotByWho(folfoxBOrCapoxBPatientsForPfs)?.let { "PFS with CAPOX-B or FOLFOX-B by WHO" to it },
        )

        val osPlots = listOfNotNull(
            OsPlot.createOsPlot(sortedPatientsByPopulationForOs)?.let { "OS by population" to it },
            plotByWho(filteredPatientsForOs)?.let { "OS by WHO" to it },
            plotByWho(folfoxiriBPatientsForOs)?.let { "OS with FOLFOXIRI-B by WHO" to it },
            plotByWho(folfoxBPatientsForOs)?.let { "OS with FOLFOX-B by WHO" to it },
            plotByWho(folfoxBOrCapoxBPatientsForOs)?.let { "OS with CAPOX-B or FOLFOX-B by WHO" to it },
        )

        val populationPlotsByTreatmentForPfs = populationDefinitions.mapNotNull { definition ->
            val filteredPatientsByTreatment = filteredPatientsForPfs.filter(definition.criteria).groupBy { (_, episode) ->
                episode.systemicTreatmentPlan!!.treatment.treatmentGroup.display
            }
            PfsPlot.createPfsPlot(filteredPatientsByTreatment)
            PfsPlot.createPfsPlot(filteredPatientsByTreatment)
                ?.let { "PFS for group ${definition.name} by treatment" to it }
        }

        val populationPlotsByTreatmentForOs = populationDefinitions.mapNotNull { definition ->
            val filteredPatientsByTreatment = filteredPatientsForOs.filter(definition.criteria).groupBy { (_, episode) ->
                episode.systemicTreatmentPlan!!.treatment.treatmentGroup.display
            }
            OsPlot.createOsPlot(filteredPatientsByTreatment)
                ?.let { "OS for group ${definition.name} by treatment" to it }
        }

        return (pfsPlots + populationPlotsByTreatmentForPfs + osPlots + populationPlotsByTreatmentForOs).toMap()
    }

    private fun plotByWho(sortedPatients: List<DiagnosisAndEpisode>): Plot? {
        val patientsByWho = sortedPatients.groupBy { (_, episode) -> "WHO ${episode.whoStatusPreTreatmentStart}" }
            .filter { (key, _) -> key != "WHO null" }
            .toSortedMap()
        return PfsPlot.createPfsPlot(patientsByWho)
    }
}
