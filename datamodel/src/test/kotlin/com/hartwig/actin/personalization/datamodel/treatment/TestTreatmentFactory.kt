package com.hartwig.actin.personalization.datamodel.treatment

import com.hartwig.actin.personalization.datamodel.TestDatamodelFactory

object TestTreatmentFactory {

    fun create(
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
                    treatment = systemicTreatment,
                    schemes = listOf(
                        listOf(
                            TestDatamodelFactory.drugTreatment(daysBetweenDiagnosisAndStart = daysBetweenDiagnosisAndSystemicTreatmentStart)
                        )
                    )
                )
            )
        } ?: emptyList()

        val progressionMeasures = hasProgressionEvent?.let { hasEvent ->
            if (hasEvent) {
                listOf(TestDatamodelFactory.progressionMeasure(daysSinceDiagnosis = daysBetweenDiagnosisAndProgression))
            } else {
                emptyList()
            }
        } ?: emptyList()

        return TestDatamodelFactory.treatmentEpisode(
            metastaticPresence = metastaticPresence,
            primarySurgeries = primarySurgeries,
            systemicTreatments = systemicTreatments,
            progressionMeasures = progressionMeasures
        )
    }
}