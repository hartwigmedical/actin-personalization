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
        return extractFirstMetastaticSystemicTreatment()?.let { determineDaysBetweenDiagnosisAndStart(it) }
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
        return hasEventPriorToMetastaticTreatment(
            chooseList = { it.primarySurgeries },
            daysExtractor = { surgery -> surgery.daysSinceDiagnosis })
    }

    fun hasPrimarySurgeryDuringMetastaticTreatment(): Boolean {
        return hasEventDuringMetastaticTreatment(
            chooseList = { it.primarySurgeries },
            daysExtractor = { surgery -> surgery.daysSinceDiagnosis })
    }

    fun hasGastroenterologySurgeryPriorToMetastaticTreatment(): Boolean {
        return hasEventPriorToMetastaticTreatment(
            chooseList = { it.gastroenterologyResections },
            daysExtractor = { resection -> resection.daysSinceDiagnosis })
    }

    fun hasGastroenterologySurgeryDuringMetastaticTreatment(): Boolean {
        return hasEventDuringMetastaticTreatment(
            chooseList = { it.gastroenterologyResections },
            daysExtractor = { resection -> resection.daysSinceDiagnosis })
    }

    fun hasHipecPriorToMetastaticTreatment(): Boolean {
        return hasEventPriorToMetastaticTreatment(
            chooseList = { it.hipecTreatments },
            daysExtractor = { hipec -> hipec.daysSinceDiagnosis })
    }

    fun hasHipecDuringMetastaticTreatment(): Boolean {
        return hasEventDuringMetastaticTreatment(
            chooseList = { it.hipecTreatments },
            daysExtractor = { hipec -> hipec.daysSinceDiagnosis })
    }

    fun hasPrimaryRadiotherapyPriorToMetastaticTreatment(): Boolean {
        return hasEventPriorToMetastaticTreatment(
            chooseList = { it.primaryRadiotherapies },
            daysExtractor = { rt -> rt.daysBetweenDiagnosisAndStop ?: rt.daysBetweenDiagnosisAndStart })
    }

    fun hasPrimaryRadiotherapyDuringMetastaticTreatment(): Boolean {
        return hasEventDuringMetastaticTreatment(
            chooseList = { it.primaryRadiotherapies },
            daysExtractor = { rt -> rt.daysBetweenDiagnosisAndStop ?: rt.daysBetweenDiagnosisAndStart })
    }

    fun hasSystemicTreatmentPriorToMetastaticTreatment(): Boolean {
        return hasEventPriorToMetastaticTreatment(
            chooseList = { it.systemicTreatments },
            daysExtractor = { systemic -> determineDaysBetweenDiagnosisAndStart(systemic) })
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

    fun firstMetastaticSystemicTreatmentDurationDays(): Int? {
        val firstSystemicTreatment = extractFirstMetastaticSystemicTreatment() ?: return null
        val treatmentStart = determineDaysBetweenDiagnosisAndStart(firstSystemicTreatment)
        val treatmentStop = determineDaysBetweenDiagnosisAndStop(firstSystemicTreatment)

        return if (treatmentStart == null || treatmentStop == null) {
            null
        } else {
            treatmentStop - treatmentStart
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

    private fun <T> hasEventPriorToMetastaticTreatment(chooseList: (TreatmentEpisode) -> List<T>, daysExtractor: (T) -> Int?): Boolean {
        if (extractPreMetastaticTreatmentEpisodes().any { chooseList(it).isNotEmpty() }) {
            return true
        }

        val metastaticTreatmentStartDays = determineMetastaticSystemicTreatmentStart()
        val metastaticEpisode = extractMetastaticTreatmentEpisode() ?: return false

        if (metastaticTreatmentStartDays == null) {
            return chooseList(metastaticEpisode).isNotEmpty()
        }

        return chooseList(metastaticEpisode).any { element ->
            daysExtractor(element)?.let { it < metastaticTreatmentStartDays } ?: false
        }
    }

    private fun <T> hasEventDuringMetastaticTreatment(chooseList: (TreatmentEpisode) -> List<T>, daysExtractor: (T) -> Int?): Boolean {
        val metastaticTreatmentStartDays = determineMetastaticSystemicTreatmentStart()
        val metastaticEpisode = extractMetastaticTreatmentEpisode() ?: return false

        if (metastaticTreatmentStartDays == null) {
            return chooseList(metastaticEpisode).isNotEmpty()
        }

        return chooseList(metastaticEpisode).any { element -> daysExtractor(element)?.let { it >= metastaticTreatmentStartDays } ?: false }
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