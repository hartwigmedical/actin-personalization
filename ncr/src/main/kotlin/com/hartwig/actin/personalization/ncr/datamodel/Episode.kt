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
    val stageCT: StageCT?
    val stageCN: StageCN?
    val stageCM: StageCM?
    val stageCTNM: StageTNM?
    val stagePT: StagePT?
    val stagePN: StagePN?
    val stagePM: StagePM?
    val stagePTNM: StageTNM?
    val stageTNM: StageTNM?
    val numberOfInvestigatedLymphNodes: Int?
    val numberOfPositiveLymphNodes: Int?
}
