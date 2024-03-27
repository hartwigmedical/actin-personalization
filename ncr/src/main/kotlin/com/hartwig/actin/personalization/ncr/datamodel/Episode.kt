package com.hartwig.actin.personalization.ncr.datamodel

interface Episode {
    val id: Int
    val order: Int
    val distantMetastasesStatus: DistantMetastasesStatus
    val whoStatusPreTreatmentStart: Int?
    val asaClassificationPreSurgeryOrEndoscopy: AsaClassificationPreSurgeryOrEndoscopy?

    val tumorIncidenceYear: Int
    val tumorBasisOfDiagnosis: TumorBasisOfDiagnosis
    val tumorLocation: TumorLocation
    val tumorDifferentiationGrade: TumorDifferentiationGrade?
    val stageCT: StageT?
    val stageCN: StageN?
    val stageCM: StageM?
    val stageCTNM: StageTNM?
    val stagePT: StageT?
    val stagePN: StageN?
    val stagePM: StageM?
    val stagePTNM: StageTNM?
    val stageTNM: StageTNM?
    val numberOfInvestigatedLymphNodes: Int?
    val numberOfPositiveLymphNodes: Int?
}
