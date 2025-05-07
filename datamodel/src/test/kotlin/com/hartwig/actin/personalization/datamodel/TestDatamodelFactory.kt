package com.hartwig.actin.personalization.datamodel

import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasure
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureFollowUpEvent
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureType
import com.hartwig.actin.personalization.datamodel.outcome.SurvivalMeasurement
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticPresence
import com.hartwig.actin.personalization.datamodel.treatment.PrimarySurgery
import com.hartwig.actin.personalization.datamodel.treatment.ReasonRefrainmentFromTreatment
import com.hartwig.actin.personalization.datamodel.treatment.SurgeryType
import com.hartwig.actin.personalization.datamodel.treatment.SystemicTreatment
import com.hartwig.actin.personalization.datamodel.treatment.Treatment
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentEpisode

object TestDatamodelFactory {

    fun patient(tumor: Tumor): ReferencePatient {
        return TestReferencePatientFactory.emptyReferencePatient().copy(tumors = listOf(tumor))
    }

    fun tumor(
        ageAtDiagnosis: Int = 75,
        isAlive: Boolean = false,
        daysBetweenDiagnosisAndSurvivalMeasurement: Int = 100,
        metastaticPresenceUnderSystemicTreatment: MetastaticPresence = MetastaticPresence.AT_START,
        systemicTreatment: Treatment? = null,
        systemicTreatmentStart: Int? = null,
        primarySurgeryType: SurgeryType? = null,
        daysBetweenDiagnosisAndProgression: Int? = null,
        hasProgressionEvent: Boolean? = null
    ): Tumor {
        val treatmentEpisode = treatmentEpisode(
            metastaticPresence = metastaticPresenceUnderSystemicTreatment,
            systemicTreatment = systemicTreatment,
            daysBetweenDiagnosisAndSystemicTreatmentStart = systemicTreatmentStart,
            primarySurgeryType = primarySurgeryType,
            daysBetweenDiagnosisAndProgression = daysBetweenDiagnosisAndProgression,
            hasProgressionEvent = hasProgressionEvent
        )

        return tumor(ageAtDiagnosis, isAlive, daysBetweenDiagnosisAndSurvivalMeasurement, treatmentEpisode)
    }

    fun tumor(
        ageAtDiagnosis: Int = 75,
        isAlive: Boolean = true,
        daysBetweenDiagnosisAndSurvivalMeasurement: Int = 100,
        treatmentEpisode: TreatmentEpisode? = null
    ): Tumor {
        return TestReferencePatientFactory.minimalReferencePatient().tumors.first().copy(
            ageAtDiagnosis = ageAtDiagnosis,
            latestSurvivalMeasurement = SurvivalMeasurement(
                daysSinceDiagnosis = daysBetweenDiagnosisAndSurvivalMeasurement,
                isAlive = isAlive
            ),
            treatmentEpisodes = treatmentEpisode?.let { listOf(it) } ?: emptyList()
        )
    }

    fun treatmentEpisode(
        metastaticPresence: MetastaticPresence = MetastaticPresence.AT_START,
        systemicTreatment: Treatment? = null,
        daysBetweenDiagnosisAndSystemicTreatmentStart: Int? = null,
        primarySurgeryType: SurgeryType? = null,
        hasProgressionEvent: Boolean? = null,
        daysBetweenDiagnosisAndProgression: Int? = null
    ): TreatmentEpisode {
        val primarySurgeries = primarySurgeryType?.let {
            listOf(
                PrimarySurgery(
                    daysSinceDiagnosis = null,
                    type = it,
                    technique = null,
                    urgency = null,
                    radicality = null,
                    circumferentialResectionMargin = null,
                    anastomoticLeakageAfterSurgery = null,
                    hospitalizationDurationDays = null
                )
            )
        } ?: emptyList()

        val systemicTreatments = systemicTreatment?.let {
            listOf(
                SystemicTreatment(
                    daysBetweenDiagnosisAndStart = daysBetweenDiagnosisAndSystemicTreatmentStart,
                    daysBetweenDiagnosisAndStop = null,
                    treatment = systemicTreatment,
                    schemes = emptyList()
                )
            )
        } ?: emptyList()

        val progressionMeasures = hasProgressionEvent?.let { hasEvent ->
            if (hasEvent) listOf(progressionMeasure(daysSinceDiagnosis = daysBetweenDiagnosisAndProgression)) else emptyList()
        } ?: emptyList()

        return TreatmentEpisode(
            metastaticPresence = metastaticPresence,
            reasonRefrainmentFromTreatment = ReasonRefrainmentFromTreatment.NOT_APPLICABLE,
            gastroenterologyResections = emptyList(),
            primarySurgeries = primarySurgeries,
            metastaticSurgeries = emptyList(),
            hipecTreatments = emptyList(),
            primaryRadiotherapies = emptyList(),
            metastaticRadiotherapies = emptyList(),
            systemicTreatments = systemicTreatments,
            responseMeasures = emptyList(),
            progressionMeasures = progressionMeasures
        )
    }

    fun progressionMeasure(
        daysSinceDiagnosis: Int? = null,
        type: ProgressionMeasureType = ProgressionMeasureType.PROGRESSION,
        followUpEvent: ProgressionMeasureFollowUpEvent? = null
    ): ProgressionMeasure {
        return ProgressionMeasure(
            daysSinceDiagnosis = daysSinceDiagnosis,
            type = type,
            followUpEvent = followUpEvent
        )
    }
}