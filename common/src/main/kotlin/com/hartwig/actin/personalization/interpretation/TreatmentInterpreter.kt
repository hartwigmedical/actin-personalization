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
        return extractMetastaticTreatmentEpisode()?.systemicTreatments?.mapNotNull { determineDaysBetweenDiagnosisAndStart(it) }
            ?.minOfOrNull { it }
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

    fun hasPrimarySurgeryPriorToMetastaticTreatment(metastaticTreatmentStartDays: Int?): Boolean {
        return hasEventPrior(metastaticTreatmentStartDays, chooseList = { it.primarySurgeries }, daysExtractor = { surgery -> surgery.daysSinceDiagnosis })
    }

    fun hasPrimarySurgeryDuringMetastaticTreatment(metastaticTreatmentStartDays: Int?): Boolean {
        return hasEventDuring(metastaticTreatmentStartDays, chooseList = { it.primarySurgeries }, daysExtractor = { surgery -> surgery.daysSinceDiagnosis })
    }

    fun hasGastroenterologySurgeryPriorToMetastaticTreatment(metastaticTreatmentStartDays: Int?): Boolean {
        return hasEventPrior(metastaticTreatmentStartDays, chooseList = { it.gastroenterologyResections }, daysExtractor = { resection -> resection.daysSinceDiagnosis })
    }

    fun hasGastroenterologySurgeryDuringMetastaticTreatment(metastaticTreatmentStartDays: Int?): Boolean {
        return hasEventDuring(metastaticTreatmentStartDays, chooseList = { it.gastroenterologyResections }, daysExtractor = { resection -> resection.daysSinceDiagnosis })
    }

    fun hasHipecPriorToMetastaticTreatment(metastaticTreatmentStartDays: Int?): Boolean {
        return hasEventPrior(metastaticTreatmentStartDays, chooseList = { it.hipecTreatments }, daysExtractor = { hipec -> hipec.daysSinceDiagnosis })
    }

    fun hasHipecDuringMetastaticTreatment(metastaticTreatmentStartDays: Int?): Boolean {
        return hasEventDuring(metastaticTreatmentStartDays, chooseList = { it.hipecTreatments }, daysExtractor = { hipec -> hipec.daysSinceDiagnosis })
    }

    fun hasPrimaryRadiotherapyPriorToMetastaticTreatment(metastaticTreatmentStartDays: Int?): Boolean {
        return hasEventPrior(metastaticTreatmentStartDays, chooseList = { it.primaryRadiotherapies }, daysExtractor = { rt -> rt.daysBetweenDiagnosisAndStart })
    }

    fun hasPrimaryRadiotherapyDuringMetastaticTreatment(metastaticTreatmentStartDays: Int?): Boolean {
        return hasEventDuring(metastaticTreatmentStartDays, chooseList = { it.primaryRadiotherapies }, daysExtractor = { rt -> rt.daysBetweenDiagnosisAndStart })
    }

    fun hasSystemicTreatmentPriorToMetastaticTreatment(metastaticTreatmentStartDays: Int?): Boolean {
        return hasEventPrior(metastaticTreatmentStartDays, chooseList = { it.systemicTreatments }, daysExtractor = { systemic -> determineDaysBetweenDiagnosisAndStart(systemic) })
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
        val treatmentStart = determineDaysBetweenDiagnosisAndStart(firstSystemicTreatment)
        val treatmentStop = determineDaysBetweenDiagnosisAndStop(firstSystemicTreatment)

        return if (treatmentStart == null || treatmentStop == null) {
            null
        } else {
            treatmentStart - treatmentStop
        }
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

    private fun <T> hasEventPrior(
        metastaticTreatmentStartDays: Int?,
        chooseList: (TreatmentEpisode) -> List<T>,
        daysExtractor: (T) -> Int?
    ): Boolean {
        val episode = extractMetastaticTreatmentEpisode() ?: return false

        if (extractPreMetastaticTreatmentEpisodes().any { chooseList(it).isNotEmpty() }) { return true }

        if (metastaticTreatmentStartDays == null) { return chooseList(episode).isNotEmpty() }

        return chooseList(episode).any { element ->
            daysExtractor(element)?.let { it < metastaticTreatmentStartDays } ?: false
        }
    }

    private fun <T> hasEventDuring(
        metastaticTreatmentStartDays: Int?,
        chooseList: (TreatmentEpisode) -> List<T>,
        daysExtractor: (T) -> Int?
    ): Boolean {
        val episode = extractMetastaticTreatmentEpisode() ?: return false

        if (metastaticTreatmentStartDays == null) { return chooseList(episode).isNotEmpty() }

        return chooseList(episode).any { element -> daysExtractor(element)?.let { it >= metastaticTreatmentStartDays } ?: false }
    }


    private fun firstProgressionAfterSystemicTreatmentStart(treatmentEpisode: TreatmentEpisode): ProgressionMeasure? {
        val systemicTreatment = treatmentEpisode.systemicTreatments.firstOrNull() ?: return null
        val startOfTreatment = determineDaysBetweenDiagnosisAndStart(systemicTreatment)

        return treatmentEpisode.progressionMeasures
            .filter { it.type == ProgressionMeasureType.PROGRESSION }
            .filter { startOfTreatment == null || it.daysSinceDiagnosis?.let { days -> days > startOfTreatment } ?: false }
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

    private fun determineDaysBetweenDiagnosisAndStart(systemicTreatment: SystemicTreatment): Int? {
        return systemicTreatment.schemes.flatten().mapNotNull { it.daysBetweenDiagnosisAndStart }.minOfOrNull { it }
    }

    private fun determineDaysBetweenDiagnosisAndStop(systemicTreatment: SystemicTreatment): Int? {
        return systemicTreatment.schemes.flatten().mapNotNull { it.daysBetweenDiagnosisAndStop }.maxOfOrNull { it }
    }
}