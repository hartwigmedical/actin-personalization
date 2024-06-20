package com.hartwig.actin.personalization.similarity.report

import com.hartwig.actin.personalization.datamodel.Diagnosis
import com.hartwig.actin.personalization.datamodel.DistantMetastasesStatus
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.LocationGroup
import com.hartwig.actin.personalization.datamodel.PatientRecord
import com.hartwig.actin.personalization.datamodel.Treatment

typealias DiagnosisAndEpisode = Pair<Diagnosis, Episode>

private fun Episode.doesNotIncludeAdjuvantOrNeoadjuvantTreatment(): Boolean {
    return !hasHadPreSurgerySystemicChemotherapy &&
            !hasHadPostSurgerySystemicChemotherapy &&
            !hasHadPreSurgerySystemicTargetedTherapy &&
            !hasHadPostSurgerySystemicTargetedTherapy
}

class PatientPopulationBreakdown(
    private val patientsByTreatment: Map<Treatment, List<DiagnosisAndEpisode>>,
    private val columnDefinitions: List<Pair<String, (DiagnosisAndEpisode) -> Boolean>>
) {
    fun treatmentDecisionTable(): TableContent {
        return TreatmentDecisionTable.decisionTable(patientsByTreatment, columnDefinitions)
    }

    fun pfsTable(): TableContent {
        return PfsTable.pfsTable(patientsByTreatment, columnDefinitions)
    }

    companion object {
        fun createForCriteria(
            patients: List<PatientRecord>, age: Int, whoStatus: Int, hasRasMutation: Boolean, metastasisLocationGroups: Set<LocationGroup>
        ): PatientPopulationBreakdown {
            val minAge = age - 5
            val maxAge = age + 5
            val columnDefinitions: List<Pair<String, (DiagnosisAndEpisode) -> Boolean>> = listOf(
                "All" to { true },
                "Age $minAge-${maxAge}y" to { it.first.ageAtDiagnosis in minAge..maxAge },
                "WHO $whoStatus" to { it.second.whoStatusPreTreatmentStart == whoStatus },
                "RAS ${if (hasRasMutation) "positive" else "negative"}" to { it.first.hasRasMutation == hasRasMutation },
                "${formatLocationGroups(metastasisLocationGroups)} lesions" to { (_, episode) ->
                    episode.systemicTreatmentPlan?.intervalTumorIncidenceTreatmentPlanStart?.let { planStart ->
                        val groups = episode.metastases.filter { metastasis ->
                            metastasis.intervalTumorIncidenceMetastasisDetection?.let { it < planStart } == true
                        }
                            .map { it.location.locationGroup.topLevelGroup() }
                            .toSet()
                        groups == metastasisLocationGroups

                    } == true
                }
            )

            val referencePop = patients.flatMap(PatientRecord::tumorEntries).map { (diagnosis, episodes) ->
                diagnosis to episodes.single { it.order == 1 }
            }
                .filter { (_, episode) ->
                    episode.distantMetastasesStatus == DistantMetastasesStatus.AT_START &&
                            episode.systemicTreatmentPlan?.treatment?.let { it != Treatment.OTHER } == true &&
                            episode.surgeries.isEmpty() &&
                            episode.doesNotIncludeAdjuvantOrNeoadjuvantTreatment()
                }

            val patientsByTreatment = referencePop.groupBy { (_, episode) -> episode.systemicTreatmentPlan!!.treatment }

            return PatientPopulationBreakdown(patientsByTreatment, columnDefinitions)
        }

        private fun formatLocationGroups(locationGroups: Set<LocationGroup>): String {
            val locationNames = locationGroups.map(LocationGroup::display)
            return when (locationNames.size) {
                0 -> "No"
                1 -> "${locationNames.single()} only"
                else -> (locationNames.dropLast(2) + locationNames.takeLast(2).joinToString(" & ")).joinToString(separator = ", ")
            }
            return locationNames.joinToString(separator = ", ")
        }
    }
}