package com.hartwig.actin.personalization.ncr.datamodel

interface Episode {
    val id: Int
    val order: Int
    val distantMetastasesStatus: DistantMetastasesStatus
    val whoStatusPreTreatmentStart: WhoStatusPreTreatmentStart?
    val asaClassificationPreSurgeryOrEndoscopy: AsaClassificationPreSurgeryOrEndoscopy?
}
