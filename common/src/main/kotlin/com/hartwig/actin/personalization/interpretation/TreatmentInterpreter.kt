package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasure
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureType
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticPresence
import com.hartwig.actin.personalization.datamodel.treatment.SystemicTreatment
import com.hartwig.actin.personalization.datamodel.treatment.Treatment
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentEpisode
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentGroup

class TreatmentInterpreter(private val treatmentEpisodes: List<TreatmentEpisode>) {

    fun hasMetastaticTreatment(): Boolean {
        return extractMetastaticTreatmentEpisode() != null
    }
    
    fun determineMetastaticSystemicTreatmentStart(): Int? {
        return extractMetastaticTreatmentEpisode()?.systemicTreatments?.mapNotNull { it.daysBetweenDiagnosisAndStart }?.minOfOrNull { it }
    }
    
    fun hasMetastaticTreatmentWithSpecificSystemicTreatmentOnly(): Boolean {
        val metastaticTreatmentEpisode = extractMetastaticTreatmentEpisode() ?: return false

        return with(metastaticTreatmentEpisode) {
            extractFirstSpecificMetastaticSystemicTreatment() != null &&
                    gastroenterologyResections.isEmpty() &&
                    primarySurgeries.isEmpty() &&
                    metastaticSurgeries.isEmpty() &&
                    hipecTreatments.isEmpty() &&
                    primaryRadiotherapies.isEmpty() &&
                    metastaticRadiotherapies.isEmpty()
        }
    }

    fun firstProgressionAfterSystemicTreatmentStart(): ProgressionMeasure? {
        val treatmentEpisode = extractMetastaticTreatmentEpisode() ?: return null
        val systemicTreatment = extractFirstSpecificMetastaticSystemicTreatment() ?: return null
        val startOfTreatment = systemicTreatment.daysBetweenDiagnosisAndStart

        return treatmentEpisode.progressionMeasures
            .filter { it.type == ProgressionMeasureType.PROGRESSION }
            .filter { startOfTreatment == null || it.daysSinceDiagnosis?.let { it > startOfTreatment } ?: false }
            .sortedBy { it.daysSinceDiagnosis }
            .firstOrNull()
    }
    
    fun firstSpecificMetastaticSystemicTreatmentGroup(): TreatmentGroup? {
        return extractFirstSpecificMetastaticSystemicTreatment()?.treatment?.treatmentGroup
    }

    private fun extractMetastaticTreatmentEpisode(): TreatmentEpisode? {
        return treatmentEpisodes.firstOrNull { it.metastaticPresence == MetastaticPresence.AT_START }
    }

    private fun extractFirstSpecificMetastaticSystemicTreatment(): SystemicTreatment? {
        return extractMetastaticTreatmentEpisode()?.systemicTreatments?.firstOrNull { it.treatment != Treatment.OTHER }
    }
}