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
    val stageCT: Int?
    val stageCN: Int?
    val stageCM: Int?
    val stageCTNM: StageTNM?
    val stagePT: Int?
    val stagePN: Int?
    val stagePM: Int?
    val stagePTNM: StageTNM?
    val stageTNM: StageTNM?
    val numberOfInvestigatedLymphNodes: Int?
    val numberOfPositiveLymphNodes: Int?
}
