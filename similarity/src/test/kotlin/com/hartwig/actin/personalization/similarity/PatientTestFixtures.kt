package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.datamodel.diagnosis.BasisOfDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastasesDetectionStatus
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocation
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorType
import com.hartwig.actin.personalization.datamodel.old.Diagnosis
import com.hartwig.actin.personalization.datamodel.old.DiagnosisEpisode
import com.hartwig.actin.personalization.datamodel.old.Episode
import com.hartwig.actin.personalization.datamodel.old.ReferencePatient
import com.hartwig.actin.personalization.datamodel.old.SystemicTreatmentPlan
import com.hartwig.actin.personalization.datamodel.old.TumorEntry
import com.hartwig.actin.personalization.datamodel.treatment.Treatment

val DIAGNOSIS = Diagnosis(
    consolidatedTumorType = TumorType.CRC_OTHER,
    tumorLocations = emptySet(),
    hasHadTumorDirectedSystemicTherapy = false,
    ageAtDiagnosis = 50,
    observedOsFromTumorIncidenceDays = 100,
    hadSurvivalEvent = true,
    hasHadPriorTumor = true,
    priorTumors = emptyList(),
    orderOfFirstDistantMetastasesEpisode = 1,
    isMetachronous = false
)
val EPISODE = Episode(
    id = 123,
    order = 1,
    tumorIncidenceYear = 2020,
    tumorBasisOfDiagnosis = BasisOfDiagnosis.CLINICAL_AND_DIAGNOSTIC_INVESTIGATION,
    tumorLocation = TumorLocation.COLON_NOS,
    distantMetastasesDetectionStatus = MetastasesDetectionStatus.AT_START,
    metastases = emptyList(),
    hasReceivedTumorDirectedTreatment = false,
    hasHadHipecTreatment = false,
    hasHadPreSurgeryRadiotherapy = false,
    hasHadPostSurgeryRadiotherapy = false,
    hasHadPreSurgeryChemoRadiotherapy = false,
    hasHadPostSurgeryChemoRadiotherapy = false,
    hasHadPreSurgerySystemicChemotherapy = false,
    hasHadPostSurgerySystemicChemotherapy = false,
    hasHadPreSurgerySystemicTargetedTherapy = false,
    hasHadPostSurgerySystemicTargetedTherapy = false,
    pfsMeasures = emptyList(),
    ageAtTreatmentPlanStart = 50,
    tnmCM = TnmM.M1
)
val DIAGNOSIS_EPISODE = DiagnosisEpisode(
    diagnosis = DIAGNOSIS,
    episode = EPISODE
)
val PATIENT_RECORD = ReferencePatient(
    ncrId = 123,
    sex = Sex.MALE,
    isAlive = true,
    tumorEntries = listOf(TumorEntry(DIAGNOSIS, listOf(EPISODE))),
)

fun recordWithEpisode(episode: Episode): ReferencePatient {
    return PATIENT_RECORD.copy(tumorEntries = PATIENT_RECORD.tumorEntries.map { it.copy(episodes = listOf(episode)) })
}

fun patientWithTreatment(
    treatment: Treatment,
    pfsDays: Int? = null,
    planStart: Int? = null,
    osDays: Int = 100,
    hadSurvivalEvent: Boolean? = null,
    hadProgressionEvent: Boolean? = null,
    ageAtDiagnosis: Int? = null
): DiagnosisEpisode {
    val systemicTreatmentPlan = SystemicTreatmentPlan(
        treatment = treatment,
        systemicTreatmentSchemes = emptyList(),
        intervalTumorIncidenceTreatmentPlanStartDays = planStart,
        observedPfsDays = pfsDays,
        observedOsFromTreatmentStartDays = osDays,
        hadProgressionEvent = hadProgressionEvent
    )
    val updatedEpisode = EPISODE.copy(
        systemicTreatmentPlan = systemicTreatmentPlan
    )
    val updatedDiagnosis = DIAGNOSIS.copy(
        ageAtDiagnosis = ageAtDiagnosis ?: DIAGNOSIS.ageAtDiagnosis,
        hadSurvivalEvent = hadSurvivalEvent ?: DIAGNOSIS.hadSurvivalEvent
    )
    return DiagnosisEpisode(updatedDiagnosis, updatedEpisode)
}

