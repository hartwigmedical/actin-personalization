package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.AsaClassification

data class TreatmentEntry(
    val whoStatusPreTreatmentStart: Int? = null,
    val asaClassificationPreSurgeryOrEndoscopy: AsaClassification? = null,
)
