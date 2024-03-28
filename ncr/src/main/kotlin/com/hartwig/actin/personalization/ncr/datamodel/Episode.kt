package com.hartwig.actin.personalization.ncr.datamodel

interface Episode {
    val id: Int
    val order: Int
    val whoStatusPreTreatmentStart: Int?
    val asaClassificationPreSurgeryOrEndoscopy: AsaClassificationPreSurgeryOrEndoscopy?

    val tumorIncidenceYear: Int
    val tumorBasisOfDiagnosis: TumorBasisOfDiagnosis
    val tumorLocation: TumorLocation
    val tumorDifferentiationGrade: TumorDifferentiationGrade?
    val tnmCT: TNM_T?
    val tnmCN: TNM_N?
    val tnmCM: TNM_M?
    val tnmPT: TNM_T?
    val tnmPN: TNM_N?
    val tnmPM: TNM_M?
    val stageCTNM: StageTNM?
    val stagePTNM: StageTNM?
    val stageTNM: StageTNM?
    val numberOfInvestigatedLymphNodes: Int?
    val numberOfPositiveLymphNodes: Int?

    val distantMetastasesStatus: DistantMetastasesStatus //meta_epis
    val metastases: List<Metastasis>
    val hasKnownLiverMetastases: Boolean
    val numberOfLiverMetastases: Int?
    val maximumSizeOfLiverMetastasis: Int?

    val hasDoublePrimaryTumor: Boolean?
    val mesorectalFasciaIsClear: Boolean?
    val distanceToMesorectalFascia: Int?
    val venousInvasionCategory: VenousInvasionCategory?
    val lymphaticInvasionCategory: LymphaticInvasionCategory?
    val extraMuralInvastionCategory: ExtraMuralInvasionCategory?
    val tumorRegression: TumorRegression?

    val labMeasurements: List<LabMeasurement>

    val hasReceivedTumorDirectedTreatment: Boolean
    val reasonRefrainmentFromTumorDirectedTreatment : ReasonRefrainmentFromTumorDirectedTreatment?
    val hasParticipatedInTrial: Boolean?

    val mdlResections: List<MDLResection>
    val surgeries: List<Surgery>
    val surgeriesMetastases: List<SurgeryMetastases>
    val radiotherapies: List<Radiotherapy>
    val radiotherapiesMetastases: List<RadiotherapyMetastases>
    val hasHadHipecTreatment: Boolean
    val intervalTumorIncidenceHipecTreatment: Int?
    val systemicTreatments: List<SystemicTreatment>
    val treatmentLines: List<SystemicTreatmentLine>
    val hasHadPreSurgeryRadiotherapy: Boolean
    val hasHadPostSurgeryRadiotherapy: Boolean
    val hasHadPreSurgeryChemoRadiotherapy: Boolean
    val hasHadPostSurgeryChemoRadiotherapy: Boolean
    val hasHadPreSurgerySystemicChemotherapy: Boolean
    val hasHadPostSurgerySystemicChemotherapy: Boolean
    val hasHadPreSurgerySystemicTargetedTherapy: Boolean
    val hasHadPostSurgerySystemicTargetedTherapy: Boolean

    val responseMeasure: ResponseMeasure?
    val pfsMeasures: List<PFSMeasure>
}
