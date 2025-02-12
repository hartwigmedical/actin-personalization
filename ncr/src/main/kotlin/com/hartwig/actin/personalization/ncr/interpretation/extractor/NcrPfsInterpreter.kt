package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.old.PfsMeasure
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureType

object NcrPfsInterpreter {

    fun determineObservedPfsAndProgressionEvent(
        daysUntilPlanStart: Int?,
        daysUntilPlanEnd: Int?,
        pfsMeasures: List<PfsMeasure>
    ): Pair<Int?, Boolean?> {
        val sortedPfsMeasuresAfterPlanStart = pfsMeasures.asSequence()
            .filterNot { it.intervalTumorIncidencePfsMeasureDays == null }
            .sortedBy(PfsMeasure::intervalTumorIncidencePfsMeasureDays)
            .dropWhile { daysUntilPlanStart != null && it.intervalTumorIncidencePfsMeasureDays!! < daysUntilPlanStart }
            .toList()
        val sortedPfsMeasuresAfterPlanEnd = sortedPfsMeasuresAfterPlanStart
            .dropWhile { daysUntilPlanEnd != null && it.intervalTumorIncidencePfsMeasureDays!! < daysUntilPlanEnd }

        return if (daysUntilPlanStart == null ||
            hasPfsMeasureWithUnknownInterval(pfsMeasures) ||
            hasInvalidPfsMeasureCombination(pfsMeasures)
        ) {
            Pair(null, null)
        } else {
            determineRelevantProgressionIntervalAndEvent(
                sortedPfsMeasuresAfterPlanStart,
                sortedPfsMeasuresAfterPlanEnd,
                daysUntilPlanEnd
            )
                ?.let { (daysUntilProgression, hadProgressionEvent) ->
                    daysUntilProgression - daysUntilPlanStart to hadProgressionEvent
                }
                ?: Pair(null, null)
        }
    }

    private fun hasPfsMeasureWithUnknownInterval(pfsMeasures: List<PfsMeasure>) = pfsMeasures.any {
        it.intervalTumorIncidencePfsMeasureDays == null
    }

    private fun hasInvalidPfsMeasureCombination(pfsMeasures: List<PfsMeasure>) =
        (pfsMeasures.size > 1 && pfsMeasures.any { it.type == ProgressionMeasureType.CENSOR }) ||
                pfsMeasures.dropLast(1).any { it.type == ProgressionMeasureType.DEATH }

    private fun determineRelevantProgressionIntervalAndEvent(
        sortedPfsMeasuresAfterPlanStart: List<PfsMeasure>,
        sortedPfsMeasuresAfterPlanEnd: List<PfsMeasure>,
        daysUntilPlanEnd: Int?
    ): Pair<Int, Boolean>? {
        return if (sortedPfsMeasuresAfterPlanStart.size == 1) {
            sortedPfsMeasuresAfterPlanStart.firstOrNull { it.type != ProgressionMeasureType.CENSOR }
                ?.intervalTumorIncidencePfsMeasureDays?.let { it to true }
                ?: sortedPfsMeasuresAfterPlanStart.last().intervalTumorIncidencePfsMeasureDays?.let { it to false }
        } else {
            daysUntilPlanEnd?.let {
                sortedPfsMeasuresAfterPlanEnd.firstOrNull()?.intervalTumorIncidencePfsMeasureDays?.let { it to true }
                    ?: sortedPfsMeasuresAfterPlanStart.lastOrNull()?.intervalTumorIncidencePfsMeasureDays?.let { it to true }
            }
        }
    }
}