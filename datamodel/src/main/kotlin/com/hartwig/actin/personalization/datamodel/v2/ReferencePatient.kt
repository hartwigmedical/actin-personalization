package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.Sex

data class ReferencePatient(
    val sex: Sex,
    val tumors: List<TumorEntry>
)
