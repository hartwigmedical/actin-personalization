package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.ReferencePatient
import com.hartwig.actin.personalization.datamodel.TestReferencePatientFactory
import com.hartwig.actin.personalization.datamodel.Tumor
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasure
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureType
import com.hartwig.actin.personalization.datamodel.outcome.SurvivalMeasurement
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticPresence
import com.hartwig.actin.personalization.datamodel.treatment.PrimarySurgery
import com.hartwig.actin.personalization.datamodel.treatment.SurgeryType
import com.hartwig.actin.personalization.datamodel.treatment.SystemicTreatment
import com.hartwig.actin.personalization.datamodel.treatment.Treatment

fun patientWithTumor(tumor: Tumor): ReferencePatient {
    return TestReferencePatientFactory.emptyReferencePatient().copy(tumors = listOf(tumor))
}

fun tumorWithTreatment(
    treatment: Treatment?,
    surgeryType: SurgeryType? = null,
    metastaticPresence: MetastaticPresence = MetastaticPresence.AT_START,
    pfsDays: Int? = null,
    planStart: Int? = null,
    osDays: Int = 100,
    hadSurvivalEvent: Boolean? = null,
    hadProgressionEvent: Boolean? = null,
    ageAtDiagnosis: Int = 75
): Tumor {
    val primarySurgeries = surgeryType?.let { 
        listOf(PrimarySurgery(daysSinceDiagnosis = null,
            type = it,
            technique = null,
            urgency = null,
            radicality = null,
            circumferentialResectionMargin = null,
            anastomoticLeakageAfterSurgery = null,
            hospitalizationDurationDays = null))
    } ?: emptyList()
    
    val systemicTreatments = treatment?.let {
        listOf(
            SystemicTreatment(
                daysBetweenDiagnosisAndStart = planStart,
                daysBetweenDiagnosisAndStop = null,
                treatment = treatment,
                schemes = emptyList()
            )
        )
    } ?: emptyList()

    val progressionMeasures = hadProgressionEvent?.let {
        listOf(
            ProgressionMeasure(
                daysSinceDiagnosis = pfsDays,
                type = ProgressionMeasureType.PROGRESSION,
                followUpEvent = null
            )
        )
    } ?: emptyList()

    val base = TestReferencePatientFactory.minimalReferencePatient()
    
    return base.tumors.first().copy(
        ageAtDiagnosis = ageAtDiagnosis,
        latestSurvivalMeasurement = SurvivalMeasurement(
            daysSinceDiagnosis = osDays,
            isAlive = hadSurvivalEvent != false
        ),
        treatmentEpisodes = listOf(
            base.tumors.first().treatmentEpisodes.first().copy(
                metastaticPresence = metastaticPresence,
                primarySurgeries = primarySurgeries,
                systemicTreatments = systemicTreatments,
                progressionMeasures = progressionMeasures
            )
        )
    )
}

