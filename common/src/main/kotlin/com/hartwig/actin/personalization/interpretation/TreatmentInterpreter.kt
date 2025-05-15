package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasure
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureType
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticPresence
import com.hartwig.actin.personalization.datamodel.treatment.SystemicTreatment
import com.hartwig.actin.personalization.datamodel.treatment.Treatment
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentEpisode

class TreatmentInterpreter(private val treatmentEpisodes : List<TreatmentEpisode>) {

    fun hasMetastaticTreatmentEpisode() : Boolean {
        return extractMetastaticTreatmentEpisode() != null
    }
    
    fun determineSystemicTreatmentStartForMetastaticDisease() : Int? {
        return extractMetastaticTreatmentEpisode()?.systemicTreatments?.mapNotNull { it.daysBetweenDiagnosisAndStart }?.minOfOrNull { it }
    }

    fun firstProgressionAfterSystemicTreatmentStart(): ProgressionMeasure? {
        val treatmentEpisode = extractMetastaticTreatmentEpisode() ?: return null
        val systemicTreatment = extractFirstSpecificSystemicTreatment(treatmentEpisode) ?: return null
        val startOfTreatment = systemicTreatment.daysBetweenDiagnosisAndStart

        return treatmentEpisode.progressionMeasures
            .filter { it.type == ProgressionMeasureType.PROGRESSION }
            .filter { startOfTreatment == null || it.daysSinceDiagnosis?.let { it > startOfTreatment } ?: false }
            .sortedBy { it.daysSinceDiagnosis }
            .firstOrNull()
    }

    fun extractMetastaticTreatmentEpisode(): TreatmentEpisode? {
        return treatmentEpisodes.firstOrNull { it.metastaticPresence == MetastaticPresence.AT_START }
    }

    fun firstSpecificMetastaticSystemicTreatment(): SystemicTreatment? {
        return extractMetastaticTreatmentEpisode()?.let { extractFirstSpecificSystemicTreatment(it) }
    }

    fun extractFirstSpecificSystemicTreatment(treatmentEpisode: TreatmentEpisode): SystemicTreatment? {
        return treatmentEpisode.systemicTreatments.firstOrNull { it.treatment != Treatment.OTHER }
    }
}