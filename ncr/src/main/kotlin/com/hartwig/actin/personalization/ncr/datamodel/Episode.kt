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
    val metastases: List<Metastases>
    val hasKnownLiverMetastases: Boolean
    val numberOfLiverMetastases: Int?
    val maximumSizeOfLiverMetastasis: Int?


}
