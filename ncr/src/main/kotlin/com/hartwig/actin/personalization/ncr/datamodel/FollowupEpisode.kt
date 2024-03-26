package com.hartwig.actin.personalization.ncr.datamodel

data class FollowupEpisode (
    override val id: Int,
    override val order: Int,
    override val distantMetastasesStatus: DistantMetastasesStatus,
    override val whoStatusPreTreatmentStart: Int?,
    override val asaClassificationPreSurgeryOrEndoscopy: AsaClassificationPreSurgeryOrEndoscopy?,
): Episode