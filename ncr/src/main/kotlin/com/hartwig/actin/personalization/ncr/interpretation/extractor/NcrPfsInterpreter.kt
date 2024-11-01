package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.PfsMeasure
import com.hartwig.actin.personalization.datamodel.PfsMeasureType

class NcrPfsInterpreter {

    fun determineObservedPfsAndProgressionEvent(
        daysUntilPlanStart: Int?,
        daysUntilPlanEnd: Int?,
        pfsMeasures: List<PfsMeasure>
    ): Pair<Int?, Boolean?> {
        val sortedPfsMeasuresAfterPlanStart = pfsMeasures.asSequence()
            .filterNot { it.intervalTumorIncidencePfsMeasureDays == null }
            .sortedBy(PfsMeasure::intervalTumorIncidencePfsMeasureDays)
            .dropWhile { daysUntilPlanStart != null && it.intervalTumorIncidencePfsMeasureDays!! < daysUntilPlanStart }
        val sortedPfsMeasuresAfterPlanEnd = sortedPfsMeasuresAfterPlanStart
            .dropWhile { daysUntilPlanEnd != null && it.intervalTumorIncidencePfsMeasureDays!! < daysUntilPlanEnd }

        return if (daysUntilPlanStart == null ||
            hasPfsMeasureWithUnknownInterval(pfsMeasures) ||
            hasProgressionAndCensorPfsMeasures(pfsMeasures)
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

    private fun hasProgressionAndCensorPfsMeasures(pfsMeasures: List<PfsMeasure>) =
        pfsMeasures.any { it.type == PfsMeasureType.PROGRESSION || it.type == PfsMeasureType.DEATH } && pfsMeasures.any() { it.type == PfsMeasureType.CENSOR }

    private fun determineRelevantProgressionIntervalAndEvent(
        sortedPfsMeasuresAfterPlanStart: Sequence<PfsMeasure>,
        sortedPfsMeasuresAfterPlanEnd: Sequence<PfsMeasure>,
        daysUntilPlanEnd: Int?
    ): Pair<Int, Boolean>? {
        return when {
            sortedPfsMeasuresAfterPlanStart.toList().size == 1 -> {
                sortedPfsMeasuresAfterPlanStart.firstOrNull { it.type != PfsMeasureType.CENSOR }?.intervalTumorIncidencePfsMeasureDays?.let { it to true }
                    ?: sortedPfsMeasuresAfterPlanStart.lastOrNull()?.intervalTumorIncidencePfsMeasureDays?.let { it to false }
            }

            sortedPfsMeasuresAfterPlanStart.toList().size > 1 -> {
                when {
                    daysUntilPlanEnd == null -> null
                    sortedPfsMeasuresAfterPlanEnd.toList().isNotEmpty() -> sortedPfsMeasuresAfterPlanEnd.first().intervalTumorIncidencePfsMeasureDays?.let { it to true }
                    else -> sortedPfsMeasuresAfterPlanStart.last().intervalTumorIncidencePfsMeasureDays?.let { it to true }
                }
            }

            else -> null
        }
    }
}