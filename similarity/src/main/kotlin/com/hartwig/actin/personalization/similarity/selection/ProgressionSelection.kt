package com.hartwig.actin.personalization.similarity.selection

import com.hartwig.actin.personalization.datamodel.Tumor
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasure
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureType

object ProgressionSelection {

    fun firstProgressionAfterSystemicTreatmentStart(tumor: Tumor): ProgressionMeasure? {
        val treatmentEpisode = TreatmentSelection.extractMetastaticTreatmentEpisode(tumor) ?: return null
        val systemicTreatment = TreatmentSelection.extractFirstSpecificSystemicTreatment(treatmentEpisode) ?: return null
        val startOfTreatment = systemicTreatment.daysBetweenDiagnosisAndStart

        return treatmentEpisode.progressionMeasures
            .filter { it.type == ProgressionMeasureType.PROGRESSION }
            .filter { startOfTreatment == null || it.daysSinceDiagnosis?.let { it > startOfTreatment } ?: false }
            .sortedBy { it.daysSinceDiagnosis }
            .firstOrNull()
    }
}