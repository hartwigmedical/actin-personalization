package com.hartwig.actin.personalization.database

import com.hartwig.actin.personalization.datamodel.ReferencePatient
import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.datamodel.Tumor
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastasesDetectionStatus
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastaticDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.PrimaryDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.PriorTumor
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorBasisOfDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocation
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocationCategory
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorStage
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorType
import com.hartwig.actin.personalization.datamodel.outcome.SurvivalMeasure
import com.hartwig.actin.personalization.datamodel.treatment.Drug
import com.hartwig.actin.personalization.datamodel.treatment.HipecTreatment

val PATIENT_RECORDS_NO_TUMOR = listOf(
    ReferencePatient(
        sex = Sex.MALE,
        tumors = emptyList()
    ),
    ReferencePatient(
        sex = Sex.FEMALE,
        tumors = emptyList()
    )
)

private val PRIOR_TUMOR_MINIMUM = PriorTumor(
    daysBeforeDiagnosis = null,
    primaryTumorType = TumorType.GASTROINTESTINAL_STROMAL_TUMOR,
    primaryTumorLocation = TumorLocation.ABDOMEN_NOS,
    primaryTumorLocationCategory = TumorLocationCategory.DIGESTIVE_TRACT,
    primaryTumorStage = null,
    systemicDrugsReceived = emptyList()
)

private val PRIOR_TUMOR = PriorTumor(
    daysBeforeDiagnosis = 732,
    primaryTumorType = TumorType.GASTROINTESTINAL_STROMAL_TUMOR,
    primaryTumorLocation = TumorLocation.ABDOMEN_NOS,
    primaryTumorLocationCategory = TumorLocationCategory.DIGESTIVE_TRACT,
    primaryTumorStage = TumorStage.II,
    systemicDrugsReceived = listOf(Drug.IMATINIB, Drug.TEGAFUR)
)

private val PRIMARY_DIAGNOSIS = PrimaryDiagnosis(
    basisOfDiagnosis = TumorBasisOfDiagnosis.HISTOLOGICAL_CONFIRMATION,
    hasDoublePrimaryTumor = false,
    primaryTumorType = TumorType.CRC_OTHER,
    primaryTumorLocation = TumorLocation.DESCENDING_COLON
)

private val METASTATIC_DIAGNOSIS = MetastaticDiagnosis(
    distantMetastasesDetectionStatus = MetastasesDetectionStatus.AT_START,
    metastases = emptyList()
)

private val TUMOR_MINIMUM = Tumor(
    diagnosisYear = 1961,
    ageAtDiagnosis = 73,
    latestSurvivalStatus = SurvivalMeasure(daysSinceDiagnosis = 151, isAlive = true),
    priorTumors = listOf(PRIOR_TUMOR_MINIMUM, PRIOR_TUMOR),
    primaryDiagnosis = PRIMARY_DIAGNOSIS,
    metastaticDiagnosis = METASTATIC_DIAGNOSIS,
    hasReceivedTumorDirectedTreatment = false,
    hipecTreatment = HipecTreatment(daysSinceDiagnosis = null, hasHadHipecTreatment = false),
)

private val TUMOR_COMPLETE = Tumor(
    diagnosisYear = 1961,
    ageAtDiagnosis = 83,
    latestSurvivalStatus = SurvivalMeasure(daysSinceDiagnosis = 90, isAlive = true),

    priorTumors = listOf(PRIOR_TUMOR_MINIMUM, PRIOR_TUMOR),

    primaryDiagnosis = PRIMARY_DIAGNOSIS,
    metastaticDiagnosis = METASTATIC_DIAGNOSIS,

    whoAssessments = emptyList(), // TODO ADD
    asaAssessments = emptyList(), // TODO ADD
    comorbidityAssessments = emptyList(), // TODO ADD
    molecularResults = emptyList(), // TODO ADD
    labMeasurements = emptyList(), // TODO ADD

    hasReceivedTumorDirectedTreatment = false,
    reasonRefrainmentFromTumorDirectedTreatment = null, // TODO ADD
    hasParticipatedInTrial = null, // TODO add

    gastroenterologyResections = emptyList(), // TODO ADD
    primarySurgeries = emptyList(), // TODO ADD
    metastaticSurgeries = emptyList(), // TODO ADD
    hipecTreatment = HipecTreatment(daysSinceDiagnosis = null, hasHadHipecTreatment = false),
    primaryRadiotherapies = emptyList(), // TODO ADD
    metastaticRadiotherapies = emptyList(), // TODO ADD
    systemicTreatments = emptyList(), // TODO ADD

    responseMeasures = emptyList(), // TODO ADD
    progressionMeasures = emptyList() // TODO ADD
)

val PATIENT_RECORDS_COMPLETE = listOf(
    ReferencePatient(
        sex = Sex.MALE,
        tumors = listOf(TUMOR_MINIMUM),
    )
)