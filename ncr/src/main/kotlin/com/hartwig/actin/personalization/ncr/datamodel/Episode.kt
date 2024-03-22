package com.hartwig.actin.personalization.ncr.datamodel

data class Episode(
    val id: Int,
    val order: Int,
    val distantMetastasesStatus: DistantMetastasesStatus,
    val whoStatusPreTreatmentStart: WhoStatusPreTreatmentStart?,
    val cciClassificationPreTreatmentStart: CciClassificationPreTreatmentStart,
    val asaClassificationPreSurgeryOrEndoscopy: AsaClassificationPreSurgeryOrEndoscopy?,

    )
