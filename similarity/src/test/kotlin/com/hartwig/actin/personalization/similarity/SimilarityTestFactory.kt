package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.ReferenceEntry
import com.hartwig.actin.personalization.datamodel.TestReferenceEntryFactory
import com.hartwig.actin.personalization.datamodel.outcome.SurvivalMeasurement
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticPresence
import com.hartwig.actin.personalization.datamodel.treatment.SurgeryType
import com.hartwig.actin.personalization.datamodel.treatment.TestTreatmentFactory
import com.hartwig.actin.personalization.datamodel.treatment.Treatment
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentEpisode

object SimilarityTestFactory {

    fun createEntry(
        ageAtDiagnosis: Int = 75,
        isAlive: Boolean = false,
        daysBetweenDiagnosisAndSurvivalMeasurement: Int = 100,
        metastaticPresenceUnderSystemicTreatment: MetastaticPresence = MetastaticPresence.AT_START,
        systemicTreatment: Treatment? = null,
        systemicTreatmentStart: Int? = null,
        primarySurgeryType: SurgeryType? = null,
        daysBetweenDiagnosisAndProgression: Int? = null,
        hasProgressionEvent: Boolean? = null
    ): ReferenceEntry {
        val treatmentEpisode = TestTreatmentFactory.create(
            metastaticPresence = metastaticPresenceUnderSystemicTreatment,
            systemicTreatment = systemicTreatment,
            daysBetweenDiagnosisAndSystemicTreatmentStart = systemicTreatmentStart,
            primarySurgeryType = primarySurgeryType,
            daysBetweenDiagnosisAndProgression = daysBetweenDiagnosisAndProgression,
            hasProgressionEvent = hasProgressionEvent
        )

        return createEntry(ageAtDiagnosis, isAlive, daysBetweenDiagnosisAndSurvivalMeasurement, treatmentEpisode)
    }

    fun createEntry(
        ageAtDiagnosis: Int = 75,
        isAlive: Boolean = true,
        daysBetweenDiagnosisAndSurvivalMeasurement: Int = 100,
        treatmentEpisode: TreatmentEpisode? = null
    ): ReferenceEntry {
        return TestReferenceEntryFactory.minimal().copy(
            ageAtDiagnosis = ageAtDiagnosis,
            latestSurvivalMeasurement = SurvivalMeasurement(
                daysSinceDiagnosis = daysBetweenDiagnosisAndSurvivalMeasurement,
                isAlive = isAlive
            ),
            treatmentEpisodes = treatmentEpisode?.let { listOf(it) } ?: emptyList()
        )
    }
}