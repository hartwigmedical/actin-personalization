package com.hartwig.actin.personalization.datamodel.v2

data class TumorEntry(
    val diagnosisYear: Int,
    val ageAtDiagnosis: Int,

    val daysBetweenDiagnosisAndLatestAliveStatusFollowup: Int,
    val wasAliveAtLatestAliveStatusFollowup: Boolean,

    val priorTumors: List<PriorTumor>,

    val hasDoublePrimaryTumor: Boolean? = null,
    val primaryDiagnosis: PrimaryDiagnosis,
    val comorbiditiesAtDiagnosis : ComorbidityAssessment?,
    val molecularResultAtDiagnosis : MolecularResult,
    val labMeasurements: List<LabMeasurement> = emptyList(),

    val metastasisDiagnosis: MetastasisDiagnosis,


    val treatments: List<TreatmentEntry>

    /*
    val hasReceivedTumorDirectedTreatment: Boolean,
    val reasonRefrainmentFromTumorDirectedTreatment: ReasonRefrainmentFromTumorDirectedTreatment? = null,
    val hasParticipatedInTrial: Boolean? = null,

    val gastroenterologyResections: List<GastroenterologyResection> = emptyList(),
    val surgeries: List<Surgery> = emptyList(),
    val metastasesSurgeries: List<MetastasesSurgery> = emptyList(),
    val radiotherapies: List<Radiotherapy> = emptyList(),
    val metastasesRadiotherapies: List<MetastasesRadiotherapy> = emptyList(),
    val hasHadHipecTreatment: Boolean,
    val intervalTumorIncidenceHipecTreatmentDays: Int? = null,
    val hasHadPreSurgeryRadiotherapy: Boolean,
    val hasHadPostSurgeryRadiotherapy: Boolean,
    val hasHadPreSurgeryChemoRadiotherapy: Boolean,
    val hasHadPostSurgeryChemoRadiotherapy: Boolean,
    val hasHadPreSurgerySystemicChemotherapy: Boolean,
    val hasHadPostSurgerySystemicChemotherapy: Boolean,
    val hasHadPreSurgerySystemicTargetedTherapy: Boolean,
    val hasHadPostSurgerySystemicTargetedTherapy: Boolean,

    val responseMeasure: ResponseMeasure? = null,
    val pfsMeasures: List<PfsMeasure>,
    val systemicTreatmentPlan: SystemicTreatmentPlan? = null,
    val ageAtTreatmentPlanStart: Int? = null
     */
)