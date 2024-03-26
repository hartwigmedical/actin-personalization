package com.hartwig.actin.personalization.ncr.datamodel

data class FollowupEpisode (
    override val id: Int,
    override val order: Int,
    override val distantMetastasesStatus: DistantMetastasesStatus,
    override val whoStatusPreTreatmentStart: Int?,
    override val asaClassificationPreSurgeryOrEndoscopy: AsaClassificationPreSurgeryOrEndoscopy?,

    override val tumorIncidenceYear: Int,
    override val tumorBasisOfDiagnosis: TumorBasisOfDiagnosis,
    override val tumorLocation: TumorLocation,
    override val tumorDifferentiationGrade: TumorDifferentiationGrade?,
    override val stageCT: Int?,
    override val stageCN: Int?,
    override val stageCM: Int?,
    override val stageCTNM: StageTNM?,
    override val stagePT: Int?,
    override val stagePN: Int?,
    override val stagePM: Int?,
    override val stagePTNM: StageTNM?,
    override val stageTNM: StageTNM?,
    override val numberOfInvestigatedLymphNodes: Int?,
    override val numberOfPositiveLymphNodes: Int?,

    ): Episode