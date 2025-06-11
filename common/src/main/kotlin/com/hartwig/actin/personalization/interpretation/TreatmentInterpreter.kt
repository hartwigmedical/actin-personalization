package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasure
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureType
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticPresence
import com.hartwig.actin.personalization.datamodel.treatment.SystemicTreatment
import com.hartwig.actin.personalization.datamodel.treatment.Treatment
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentEpisode
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentGroup

class TreatmentInterpreter(private val treatmentEpisodes: List<TreatmentEpisode>) {

    fun isMetastaticPriorToMetastaticTreatmentDecision(): Boolean {
        return extractMetastaticTreatmentEpisode().metastaticPresence == MetastaticPresence.AT_START
    }

    fun determineMetastaticSystemicTreatmentStart(): Int? {
        return extractFirstMetastaticSystemicTreatment()?.let { determineDaysBetweenDiagnosisAndStart(it) }
    }

    fun reasonRefrainmentFromMetastaticTreatment(): String {
        return extractMetastaticTreatmentEpisode().reasonRefrainmentFromTreatment.name
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
        return extractMetastaticTreatmentEpisode().metastaticSurgeries.isNotEmpty()
    }

    fun hasMetastaticRadiotherapy(): Boolean {
        return extractMetastaticTreatmentEpisode().metastaticRadiotherapies.isNotEmpty()
    }

    fun metastaticSystemicTreatmentCount(): Int {
        return extractMetastaticTreatmentEpisode().systemicTreatments.size
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
            1 + treatmentStop - treatmentStart
        }
    }

    fun hasPostMetastaticTreatmentWithSystemicTreatmentOnly(): Boolean {
        val isMetastaticPriorToMetastaticTreatmentDecision = isMetastaticPriorToMetastaticTreatmentDecision()
        val hasSystemicTreatment = firstMetastaticSystemicTreatment() != null

        val hasGastroenterologyResectionDuringMetastaticTreatment = hasGastroenterologySurgeryDuringMetastaticTreatment()
        val hasPrimarySurgeryDuringMetastaticTreatment = hasPrimarySurgeryDuringMetastaticTreatment()
        val hasMetastaticSurgery = hasMetastaticSurgery()
        val hasHipecDuringMetastaticTreatment = hasHipecDuringMetastaticTreatment()
        val hasPrimaryRadiotherapyDuringMetastaticTreatment = hasPrimaryRadiotherapyDuringMetastaticTreatment()
        val hasMetastaticRadiotherapy = hasMetastaticRadiotherapy()

        val hasOtherInterventionDuringMetastaticTreatment =
            hasGastroenterologyResectionDuringMetastaticTreatment || hasPrimarySurgeryDuringMetastaticTreatment ||
                    hasMetastaticSurgery || hasHipecDuringMetastaticTreatment ||
                    hasPrimaryRadiotherapyDuringMetastaticTreatment || hasMetastaticRadiotherapy

        return isMetastaticPriorToMetastaticTreatmentDecision && hasSystemicTreatment && !hasOtherInterventionDuringMetastaticTreatment
    }

    fun hasProgressionEventAfterMetastaticSystemicTreatmentStart(): Boolean? {
        val systemicTreatmentStart = determineMetastaticSystemicTreatmentStart() ?: return null

        return firstProgressionAfterSpecificMoment(
            treatmentEpisode = extractMetastaticTreatmentEpisode(),
            minDaysSinceDiagnosis = systemicTreatmentStart
        ) != null
    }

    fun daysBetweenProgressionAndMetastaticSystemicTreatmentStart(): Int? {
        val systemicTreatmentStart = determineMetastaticSystemicTreatmentStart() ?: return null
        val progression = daysBetweenProgressionAndPrimaryDiagnosis() ?: return null

        return progression - systemicTreatmentStart
    }

    fun daysBetweenProgressionAndPrimaryDiagnosis(): Int? {
        val systemicTreatmentStart = determineMetastaticSystemicTreatmentStart() ?: return null

        return firstProgressionAfterSpecificMoment(
            treatmentEpisode = extractMetastaticTreatmentEpisode(),
            minDaysSinceDiagnosis = systemicTreatmentStart
        )?.daysSinceDiagnosis
    }

    private fun firstProgressionAfterSpecificMoment(treatmentEpisode: TreatmentEpisode, minDaysSinceDiagnosis: Int): ProgressionMeasure? {
        return treatmentEpisode.progressionMeasures
            .filter { it.type == ProgressionMeasureType.PROGRESSION }
            .filter { it.daysSinceDiagnosis?.let { days -> days > minDaysSinceDiagnosis } ?: false }
            .sortedBy { it.daysSinceDiagnosis }
            .firstOrNull()
    }

    private fun <T> hasEventPriorToMetastaticTreatment(chooseList: (TreatmentEpisode) -> List<T>, daysExtractor: (T) -> Int?): Boolean {
        if (extractPreMetastaticTreatmentEpisodes().any { chooseList(it).isNotEmpty() }) {
            return true
        }

        val metastaticTreatmentStartDays = determineMetastaticSystemicTreatmentStart()
        val metastaticEpisode = extractMetastaticTreatmentEpisode()

        if (metastaticTreatmentStartDays == null) {
            return chooseList(metastaticEpisode).isNotEmpty()
        }

        return chooseList(metastaticEpisode).any { element -> daysExtractor(element)?.let { it < metastaticTreatmentStartDays } ?: true }
    }

    private fun <T> hasEventDuringMetastaticTreatment(chooseList: (TreatmentEpisode) -> List<T>, daysExtractor: (T) -> Int?): Boolean {
        val metastaticTreatmentStartDays = determineMetastaticSystemicTreatmentStart()
        val metastaticEpisode = extractMetastaticTreatmentEpisode()

        if (metastaticTreatmentStartDays == null) {
            return chooseList(metastaticEpisode).isNotEmpty()
        }

        return chooseList(metastaticEpisode).any { element -> daysExtractor(element)?.let { it >= metastaticTreatmentStartDays } ?: true }
    }

    private fun extractMetastaticTreatmentEpisode(): TreatmentEpisode {
        val metastaticEpisodes =
            treatmentEpisodes.filter {
                it.metastaticPresence == MetastaticPresence.AT_START ||
                        it.metastaticPresence == MetastaticPresence.AT_PROGRESSION
            }

        if (metastaticEpisodes.size != 1) {
            throw IllegalStateException("There should be exactly one metastatic treatment episode")
        }

        return metastaticEpisodes.first()
    }

    private fun extractPreMetastaticTreatmentEpisodes(): List<TreatmentEpisode> {
        return treatmentEpisodes.filter { it.metastaticPresence == MetastaticPresence.ABSENT }
    }

    private fun extractFirstMetastaticSystemicTreatment(): SystemicTreatment? {
        return extractMetastaticTreatmentEpisode().systemicTreatments.firstOrNull()
    }

    private fun determineDaysBetweenDiagnosisAndStart(systemicTreatment: SystemicTreatment): Int? {
        return systemicTreatment.schemes.flatten().mapNotNull { it.daysBetweenDiagnosisAndStart }.minOfOrNull { it }
    }

    private fun determineDaysBetweenDiagnosisAndStop(systemicTreatment: SystemicTreatment): Int? {
        return systemicTreatment.schemes.flatten().mapNotNull { it.daysBetweenDiagnosisAndStop }.maxOfOrNull { it }
    }
}