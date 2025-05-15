package com.hartwig.actin.personalization.selection

import com.hartwig.actin.personalization.datamodel.ReferenceEntry
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasure
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureType

object ProgressionSelection {

    fun firstProgressionAfterSystemicTreatmentStart(entry: ReferenceEntry): ProgressionMeasure? {
        val treatmentEpisode = TreatmentSelection.extractMetastaticTreatmentEpisode(entry.treatmentEpisodes) ?: return null
        val systemicTreatment = TreatmentSelection.extractFirstSpecificSystemicTreatment(treatmentEpisode) ?: return null
        val startOfTreatment = systemicTreatment.daysBetweenDiagnosisAndStart

        return treatmentEpisode.progressionMeasures
            .filter { it.type == ProgressionMeasureType.PROGRESSION }
            .filter { startOfTreatment == null || it.daysSinceDiagnosis?.let { it > startOfTreatment } ?: false }
            .sortedBy { it.daysSinceDiagnosis }
            .firstOrNull()
    }
}