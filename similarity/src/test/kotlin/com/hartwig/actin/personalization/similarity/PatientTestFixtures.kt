package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.ReferencePatient
import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.datamodel.TestReferencePatientFactory
import com.hartwig.actin.personalization.datamodel.Tumor
import com.hartwig.actin.personalization.datamodel.diagnosis.BasisOfDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastasesDetectionStatus
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmM
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocation
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorType
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasure
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureType
import com.hartwig.actin.personalization.datamodel.outcome.SurvivalMeasurement
import com.hartwig.actin.personalization.datamodel.treatment.SystemicTreatment
import com.hartwig.actin.personalization.datamodel.treatment.Treatment

//val DIAGNOSIS = Diagnosis(
//    consolidatedTumorType = TumorType.CRC_OTHER,
//    tumorLocations = emptySet(),
//    hasHadTumorDirectedSystemicTherapy = false,
//    ageAtDiagnosis = 50,
//    observedOsFromTumorIncidenceDays = 100,
//    hadSurvivalEvent = true,
//    hasHadPriorTumor = true,
//    priorTumors = emptyList(),
//    orderOfFirstDistantMetastasesEpisode = 1,
//    isMetachronous = false
//)
//val EPISODE = Episode(
//    id = 123,
//    order = 1,
//    tumorIncidenceYear = 2020,
//    tumorBasisOfDiagnosis = BasisOfDiagnosis.CLINICAL_AND_DIAGNOSTIC_INVESTIGATION,
//    tumorLocation = TumorLocation.COLON_NOS,
//    distantMetastasesDetectionStatus = MetastasesDetectionStatus.AT_START,
//    metastases = emptyList(),
//    hasReceivedTumorDirectedTreatment = false,
//    hasHadHipecTreatment = false,
//    hasHadPreSurgeryRadiotherapy = false,
//    hasHadPostSurgeryRadiotherapy = false,
//    hasHadPreSurgeryChemoRadiotherapy = false,
//    hasHadPostSurgeryChemoRadiotherapy = false,
//    hasHadPreSurgerySystemicChemotherapy = false,
//    hasHadPostSurgerySystemicChemotherapy = false,
//    hasHadPreSurgerySystemicTargetedTherapy = false,
//    hasHadPostSurgerySystemicTargetedTherapy = false,
//    pfsMeasures = emptyList(),
//    ageAtTreatmentPlanStart = 50,
//    tnmCM = TnmM.M1
//)
//val DIAGNOSIS_EPISODE = DiagnosisEpisode(
//    diagnosis = DIAGNOSIS,
//    episode = EPISODE
//)
//val PATIENT_RECORD = ReferencePatient(
//    ncrId = 123,
//    sex = Sex.MALE,
//    isAlive = true,
//    tumorEntries = listOf(TumorEntry(DIAGNOSIS, listOf(EPISODE))),
//)

fun patientWithTumor(tumor: Tumor): ReferencePatient {
    return TestReferencePatientFactory.emptyReferencePatient().copy(tumors = listOf(tumor))
}

fun tumorWithTreatment(
    treatment: Treatment,
    pfsDays: Int? = null,
    planStart: Int? = null,
    osDays: Int = 100,
    hadSurvivalEvent: Boolean? = null,
    hadProgressionEvent: Boolean? = null,
    ageAtDiagnosis: Int = 75
): Tumor {
    val base = TestReferencePatientFactory.minimalReferencePatient()

    val systemicTreatments = listOf(
        SystemicTreatment(
            daysBetweenDiagnosisAndStart = planStart,
            daysBetweenDiagnosisAndStop = null,
            treatment = treatment,
            schemes = emptyList()
        )
    )
    
    val progressionMeasures = hadProgressionEvent?.let {
        listOf(
            ProgressionMeasure(
                daysSinceDiagnosis = pfsDays,
                type = ProgressionMeasureType.PROGRESSION,
                followUpEvent = null
            )
        )
    } ?: emptyList()
    
    return base.tumors.first().copy(
        ageAtDiagnosis = ageAtDiagnosis,
        latestSurvivalMeasurement = SurvivalMeasurement(
            daysSinceDiagnosis = osDays,
            isAlive = hadSurvivalEvent != false
        ),
        treatmentEpisodes = listOf(
            base.tumors.first().treatmentEpisodes.first().copy(
                systemicTreatments = systemicTreatments,
                progressionMeasures = progressionMeasures
            )
        )
    )

}

