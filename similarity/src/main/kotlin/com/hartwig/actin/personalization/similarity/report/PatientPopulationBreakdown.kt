package com.hartwig.actin.personalization.similarity.report

import com.hartwig.actin.personalization.datamodel.Diagnosis
import com.hartwig.actin.personalization.datamodel.DistantMetastasesStatus
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.LocationGroup
import com.hartwig.actin.personalization.datamodel.PatientRecord
import com.hartwig.actin.personalization.datamodel.Treatment
import kotlin.collections.filter
import kotlin.collections.flatMap
import kotlin.collections.groupBy
import kotlin.collections.joinToString
import kotlin.collections.map
import kotlin.collections.single
import kotlin.collections.toSet
import kotlin.let
import kotlin.to

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
    fun pfsTable(): TableContent {
        return PfsTable.pfsTable(patientsByTreatment, columnDefinitions)
    }

    companion object {
        fun createForCriteria(
            patients: List<PatientRecord>, whoStatus: Int, age: Int, hasRasMutation: Boolean, metastasisLocationGroups: Set<LocationGroup>
        ): PatientPopulationBreakdown {
            val minAge = age - 5
            val maxAge = age + 5
            val columnDefinitions: List<Pair<String, (DiagnosisAndEpisode) -> Boolean>> = listOf(
                "All" to { true },
                "WHO=$whoStatus" to { it.second.whoStatusPreTreatmentStart == whoStatus },
                "Age $minAge-${maxAge}y" to { it.first.ageAtDiagnosis in minAge..maxAge },
                "RAS ${if (hasRasMutation) "positive" else "negative"}" to { it.first.hasRasMutation == hasRasMutation },
                "${metastasisLocationGroups.joinToString(", ")} metastases" to { (_, episode) ->
                    episode.systemicTreatmentPlan?.intervalTumorIncidenceTreatmentPlanStart?.let { planStart ->
                        val groups = episode.metastases.filter { metastasis ->
                            metastasis.intervalTumorIncidenceMetastasisDetection?.let { it < planStart } == true
                        }
                            .map { it.location.locationGroup }
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
    }
}