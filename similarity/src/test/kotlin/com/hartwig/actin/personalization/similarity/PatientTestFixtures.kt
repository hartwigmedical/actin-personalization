package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.Diagnosis
import com.hartwig.actin.personalization.datamodel.DiagnosisEpisode
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.Location
import com.hartwig.actin.personalization.datamodel.MetastasesDetectionStatus
import com.hartwig.actin.personalization.datamodel.ReferencePatient
import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.datamodel.SystemicTreatmentPlan
import com.hartwig.actin.personalization.datamodel.Treatment
import com.hartwig.actin.personalization.datamodel.TumorBasisOfDiagnosis
import com.hartwig.actin.personalization.datamodel.TumorEntry
import com.hartwig.actin.personalization.datamodel.TumorType

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
    tumorBasisOfDiagnosis = TumorBasisOfDiagnosis.CLINICAL_AND_DIAGNOSTIC_INVESTIGATION,
    tumorLocation = Location.COLON_NOS,
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
    pfsMeasures = emptyList()
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

fun recordWithEpisode(
    episode: Episode,
    observedOsFromTreatmentStartDays: Int? = 100,
    hadSurvivalEvent: Boolean? = null,
    diagnosis: Diagnosis = DIAGNOSIS
): ReferencePatient {
    val updatedEpisode = episode.copy(
        systemicTreatmentPlan = episode.systemicTreatmentPlan?.copy(
            observedOsFromTreatmentStartDays = observedOsFromTreatmentStartDays
        )
    )
    return ReferencePatient(
        ncrId = PATIENT_RECORD.ncrId,
        sex = PATIENT_RECORD.sex,
        isAlive = !(hadSurvivalEvent ?: diagnosis.hadSurvivalEvent),
        tumorEntries = listOf(TumorEntry(diagnosis, listOf(updatedEpisode)))
    )
}

fun patientWithTreatment(
    treatment: Treatment,
    pfsDays: Int? = null,
    planStart: Int? = null,
    osDays: Int = 100,
    hadSurvivalEvent: Boolean? = null,
    hadProgressionEvent: Boolean? = null,
    ageAtDiagnosis: Int? = null,
    episode: Episode = EPISODE,
    diagnosis: Diagnosis = DIAGNOSIS
): DiagnosisEpisode {
    val updatedSystemicTreatmentPlan = SystemicTreatmentPlan(
        treatment = treatment,
        systemicTreatmentSchemes = emptyList(),
        intervalTumorIncidenceTreatmentPlanStartDays = planStart,
        observedPfsDays = pfsDays,
        observedOsFromTreatmentStartDays = osDays, // Use this field
        hadProgressionEvent = hadProgressionEvent
    )
    val updatedEpisode = episode.copy(
        systemicTreatmentPlan = updatedSystemicTreatmentPlan
    )
    val updatedDiagnosis = diagnosis.copy(
        ageAtDiagnosis = ageAtDiagnosis ?: diagnosis.ageAtDiagnosis,
        hadSurvivalEvent = hadSurvivalEvent ?: diagnosis.hadSurvivalEvent
    )
    return DiagnosisEpisode(updatedDiagnosis, updatedEpisode)
}

