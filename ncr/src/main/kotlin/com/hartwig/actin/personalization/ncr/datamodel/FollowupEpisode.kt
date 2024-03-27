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
    override val tnmCT: TNM_T?,
    override val tnmCN: TNM_N?,
    override val tnmCM: TNM_M?,
    override val tnmPT: TNM_T?,
    override val tnmPN: TNM_N?,
    override val tnmPM: TNM_M?,
    override val stageCTNM: StageTNM?,
    override val stagePTNM: StageTNM?,
    override val stageTNM: StageTNM?,
    override val numberOfInvestigatedLymphNodes: Int?,
    override val numberOfPositiveLymphNodes: Int?,

    ): Episode