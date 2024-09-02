package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class Episode(
    val id: Int,
    val order: Int,
    val whoStatusPreTreatmentStart: Int? = null,
    val asaClassificationPreSurgeryOrEndoscopy: AsaClassification? = null,

    val tumorIncidenceYear: Int,
    val tumorBasisOfDiagnosis: TumorBasisOfDiagnosis,
    val tumorLocation: Location,
    val tumorDifferentiationGrade: TumorDifferentiationGrade? = null,
    val tnmCT: TnmT? = null,
    val tnmCN: TnmN? = null,
    val tnmCM: TnmM? = null,
    val tnmPT: TnmT? = null,
    val tnmPN: TnmN? = null,
    val tnmPM: TnmM? = null,
    val stageCTNM: StageTnm? = null,
    val stagePTNM: StageTnm? = null,
    val stageTNM: StageTnm? = null,
    val investigatedLymphNodesNumber: Int? = null,
    val positiveLymphNodesNumber: Int? = null,

    val distantMetastasesDetectionStatus: MetastasesDetectionStatus,
    val metastases: List<Metastasis>,
    val numberOfLiverMetastases: NumberOfLiverMetastases? = null,
    val maximumSizeOfLiverMetastasisMm: Int? = null,

    val hasDoublePrimaryTumor: Boolean? = null,
    val mesorectalFasciaIsClear: Boolean? = null,
    val distanceToMesorectalFasciaMm: Int? = null,
    val hasVenousInvasion: Boolean? = null,
    val venousInvasionDescription: VenousInvasionDescription? = null,
    val lymphaticInvasionCategory: LymphaticInvasionCategory? = null,
    val extraMuralInvasionCategory: ExtraMuralInvasionCategory? = null,
    val tumorRegression: TumorRegression? = null,

    val labMeasurements: List<LabMeasurement> = emptyList(),

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
    val systemicTreatmentPlan: SystemicTreatmentPlan? = null
)