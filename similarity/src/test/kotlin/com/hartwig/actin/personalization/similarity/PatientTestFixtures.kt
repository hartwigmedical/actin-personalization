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
    priorTumors = emptyList()
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

fun recordWithEpisode(diagnosis: Diagnosis, episode: Episode): ReferencePatient {
    return PATIENT_RECORD.copy(
        tumorEntries = listOf(TumorEntry(diagnosis, listOf(episode))),
        isAlive = !diagnosis.hadSurvivalEvent!!
    )
}

fun patientWithTreatment(
    treatment: Treatment,
    pfs: Int? = null,
    planStart: Int? = null,
    os: Int? = null,
    hadSurvivalEvent: Boolean? = null,
    hadProgressionEvent: Boolean? = null,
    diagnosis: Diagnosis = DIAGNOSIS
): DiagnosisEpisode {
    val updatedDiagnosis = diagnosis.copy(
        observedOsFromTumorIncidenceDays = os ?: diagnosis.observedOsFromTumorIncidenceDays,
        hadSurvivalEvent = hadSurvivalEvent ?: diagnosis.hadSurvivalEvent
    )
    val updatedEpisode = EPISODE.copy(
        systemicTreatmentPlan = SystemicTreatmentPlan(
            treatment = treatment,
            systemicTreatmentSchemes = emptyList(),
            intervalTumorIncidenceTreatmentPlanStartDays = planStart,
            observedPfsDays = pfs,
            hadProgressionEvent = hadProgressionEvent
        )
    )
    return DiagnosisEpisode(updatedDiagnosis, updatedEpisode)
}

fun patientWithoutTreatment(): Episode {
    return EPISODE.copy(
        systemicTreatmentPlan = null
    )
}
