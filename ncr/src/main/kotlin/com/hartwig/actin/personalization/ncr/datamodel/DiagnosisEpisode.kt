package com.hartwig.actin.personalization.ncr.datamodel

data class DiagnosisEpisode  (
    override val id: Int,
    override val order: Int,
    override val distantMetastasesStatus: DistantMetastasesStatus,
    override val whoStatusPreTreatmentStart: WhoStatusPreTreatmentStart?,
    override val asaClassificationPreSurgeryOrEndoscopy: AsaClassificationPreSurgeryOrEndoscopy?,

    val cciClassificationPreTreatmentStart: CciClassificationPreTreatmentStart

    ): Episode
