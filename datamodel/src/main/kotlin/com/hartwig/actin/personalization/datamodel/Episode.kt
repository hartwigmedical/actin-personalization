package com.hartwig.actin.personalization.datamodel

data class Episode(
    val id: Int,
    val order: Int,
    val whoStatusPreTreatmentStart: Int?,
    val asaClassificationPreSurgeryOrEndoscopy: AsaClassificationPreSurgeryOrEndoscopy?,

    val tumorIncidenceYear: Int,
    val tumorBasisOfDiagnosis: TumorBasisOfDiagnosis,
    val tumorLocation: Location,
    val tumorDifferentiationGrade: TumorDifferentiationGrade?,
    val tnmCT: TnmT?,
    val tnmCN: TnmN?,
    val tnmCM: TnmM?,
    val tnmPT: TnmT?,
    val tnmPN: TnmN?,
    val tnmPM: TnmM?,
    val stageCTNM: StageTnm?,
    val stagePTNM: StageTnm?,
    val stageTNM: StageTnm?,
    val numberOfInvestigatedLymphNodes: Int?,
    val numberOfPositiveLymphNodes: Int?,

    val distantMetastasesStatus: DistantMetastasesStatus,
    val metastases: List<Metastasis>,
    val numberOfLiverMetastases: NumberOfLiverMetastases?,
    val maximumSizeOfLiverMetastasisInMm: Int?,

    val hasDoublePrimaryTumor: Boolean?,
    val mesorectalFasciaIsClear: Boolean?,
    val distanceToMesorectalFascia: Int?,
    val venousInvasionCategory: VenousInvasionCategory?,
    val lymphaticInvasionCategory: LymphaticInvasionCategory?,
    val extraMuralInvasionCategory: ExtraMuralInvasionCategory?,
    val tumorRegression: TumorRegression?,

    val labMeasurements: List<LabMeasurement>,

    val hasReceivedTumorDirectedTreatment: Boolean,
    val reasonRefrainmentFromTumorDirectedTreatment: ReasonRefrainmentFromTumorDirectedTreatment?,
    val hasParticipatedInTrial: Boolean?,

    val gastroenterologyResections: List<GastroenterologyResection>,
    val surgeries: List<Surgery>,
    val metastasesSurgeries: List<MetastasesSurgery>,
    val radiotherapies: List<Radiotherapy>,
    val metastasesRadiotherapies: List<MetastasesRadiotherapy>,
    val hasHadHipecTreatment: Boolean,
    val intervalTumorIncidenceHipecTreatment: Int?,
    val hasHadPreSurgeryRadiotherapy: Boolean,
    val hasHadPostSurgeryRadiotherapy: Boolean,
    val hasHadPreSurgeryChemoRadiotherapy: Boolean,
    val hasHadPostSurgeryChemoRadiotherapy: Boolean,
    val hasHadPreSurgerySystemicChemotherapy: Boolean,
    val hasHadPostSurgerySystemicChemotherapy: Boolean,
    val hasHadPreSurgerySystemicTargetedTherapy: Boolean,
    val hasHadPostSurgerySystemicTargetedTherapy: Boolean,

    val responseMeasure: ResponseMeasure?,
    val systemicTreatmentPlan: SystemicTreatmentPlan?,
    val pfsMeasures: List<PfsMeasure>
)