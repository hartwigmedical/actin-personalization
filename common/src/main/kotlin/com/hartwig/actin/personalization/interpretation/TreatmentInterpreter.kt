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

    fun isMetastaticPriorToMetastaticTreatmentDecision(): Boolean {
        return extractMetastaticTreatmentEpisode()?.let { it.metastaticPresence == MetastaticPresence.AT_START } ?: false
    }

    fun determineMetastaticSystemicTreatmentStart(): Int? {
        return extractMetastaticTreatmentEpisode()?.systemicTreatments?.mapNotNull { it.daysBetweenDiagnosisAndStart }?.minOfOrNull { it }
    }

    fun hasPostMetastaticTreatmentWithSystemicTreatmentOnly(): Boolean {
        val metastaticTreatmentEpisode = extractMetastaticTreatmentEpisode() ?: return false

        return with(metastaticTreatmentEpisode) {
            metastaticPresence == MetastaticPresence.AT_START &&
                    systemicTreatments.isNotEmpty() &&
                    gastroenterologyResections.isEmpty() &&
                    primarySurgeries.isEmpty() &&
                    metastaticSurgeries.isEmpty() &&
                    hipecTreatments.isEmpty() &&
                    primaryRadiotherapies.isEmpty() &&
                    metastaticRadiotherapies.isEmpty()
        }
    }

    fun reasonRefrainmentFromTreatment(): String? {
        return extractMetastaticTreatmentEpisode()?.reasonRefrainmentFromTreatment?.name
    }

    fun hasPrimarySurgeryPriorToMetastaticTreatment(): Boolean {
        return extractPreMetastaticTreatmentEpisodes().any { it.primarySurgeries.isNotEmpty() }
    }

    fun hasPrimarySurgeryDuringMetastaticTreatment(): Boolean {
        return extractMetastaticTreatmentEpisode()?.primarySurgeries?.isNotEmpty() ?: return false
    }

    fun hasGastroenterologySurgeryPriorToMetastaticTreatment(): Boolean {
        return extractPreMetastaticTreatmentEpisodes().any { it.gastroenterologyResections.isNotEmpty() }
    }

    fun hasGastroenterologySurgeryDuringMetastaticTreatment(): Boolean {
        return extractMetastaticTreatmentEpisode()?.gastroenterologyResections?.isNotEmpty() ?: return false
    }

    fun hasHipecPriorToMetastaticTreatment(): Boolean {
        return extractPreMetastaticTreatmentEpisodes().any { it.hipecTreatments.isNotEmpty() }
    }

    fun hasHipecDuringMetastaticTreatment(): Boolean {
        return extractMetastaticTreatmentEpisode()?.hipecTreatments?.isNotEmpty() ?: return false
    }

    fun hasPrimaryRadiotherapyPriorToMetastaticTreatment(): Boolean {
        return extractPreMetastaticTreatmentEpisodes().any { it.primaryRadiotherapies.isNotEmpty() }
    }

    fun hasPrimaryRadiotherapyDuringMetastaticTreatment(): Boolean {
        return extractMetastaticTreatmentEpisode()?.primaryRadiotherapies?.isNotEmpty() ?: return false
    }

    fun hasSystemicTreatmentPriorToMetastaticTreatment(): Boolean {
        return extractPreMetastaticTreatmentEpisodes().any { it.systemicTreatments.isNotEmpty() }
    }

    fun hasMetastaticSurgery(): Boolean {
        return extractMetastaticTreatmentEpisode()?.metastaticSurgeries?.isNotEmpty() ?: return false
    }

    fun hasMetastaticRadiotherapy(): Boolean {
        return extractMetastaticTreatmentEpisode()?.metastaticRadiotherapies?.isNotEmpty() ?: return false
    }

    fun metastaticSystemicTreatmentCount(): Int? {
        return extractMetastaticTreatmentEpisode()?.systemicTreatments?.size ?: return null
    }

    fun firstMetastaticSystemicTreatment(): Treatment? {
        return extractFirstMetastaticSystemicTreatment()?.treatment
    }

    fun firstMetastaticSystemicTreatmentGroup(): TreatmentGroup? {
        return firstMetastaticSystemicTreatment()?.treatmentGroup
    }

    fun firstMetastaticSystemicTreatmentDuration(): Int? {
        val firstSystemicTreatment = extractFirstMetastaticSystemicTreatment() ?: return null
        if (firstSystemicTreatment.daysBetweenDiagnosisAndStart == null || firstSystemicTreatment.daysBetweenDiagnosisAndStop == null) {
            return null
        }
        return firstSystemicTreatment.daysBetweenDiagnosisAndStop!! - firstSystemicTreatment.daysBetweenDiagnosisAndStart!!
    }

    fun hasProgressionEventAfterMetastaticSystemicTreatmentStart(): Boolean? {
        val metastaticTreatmentEpisode = extractMetastaticTreatmentEpisode() ?: return null
        return firstProgressionAfterSystemicTreatmentStart(metastaticTreatmentEpisode) != null
    }

    fun daysBetweenProgressionAndMetastaticSystemicTreatmentStart(): Int? {
        val metastaticTreatmentEpisode = extractMetastaticTreatmentEpisode() ?: return null
        val treatmentStart = determineMetastaticSystemicTreatmentStart() ?: return null

        val progression = firstProgressionAfterSystemicTreatmentStart(metastaticTreatmentEpisode)?.daysSinceDiagnosis ?: return null

        return progression - treatmentStart
    }

    fun daysBetweenProgressionAndPrimaryDiagnosis(): Int? {
        val metastaticTreatmentEpisode = extractMetastaticTreatmentEpisode() ?: return null

        return firstProgressionAfterSystemicTreatmentStart(metastaticTreatmentEpisode)?.daysSinceDiagnosis
    }

    private fun firstProgressionAfterSystemicTreatmentStart(treatmentEpisode: TreatmentEpisode): ProgressionMeasure? {
        val systemicTreatment = treatmentEpisode.systemicTreatments.firstOrNull() ?: return null
        val startOfTreatment = systemicTreatment.daysBetweenDiagnosisAndStart

        return treatmentEpisode.progressionMeasures
            .filter { it.type == ProgressionMeasureType.PROGRESSION }
            .filter { startOfTreatment == null || it.daysSinceDiagnosis?.let { it > startOfTreatment } ?: false }
            .sortedBy { it.daysSinceDiagnosis }
            .firstOrNull()
    }


    private fun extractMetastaticTreatmentEpisode(): TreatmentEpisode? {
        return treatmentEpisodes.firstOrNull { it.metastaticPresence == MetastaticPresence.AT_START }
            ?: treatmentEpisodes.firstOrNull { it.metastaticPresence == MetastaticPresence.AT_PROGRESSION }
    }

    private fun extractPreMetastaticTreatmentEpisodes(): List<TreatmentEpisode> {
        return treatmentEpisodes.filter { it.metastaticPresence == MetastaticPresence.ABSENT }
    }

    private fun extractFirstMetastaticSystemicTreatment(): SystemicTreatment? {
        return extractMetastaticTreatmentEpisode()?.systemicTreatments?.firstOrNull()
    }
}