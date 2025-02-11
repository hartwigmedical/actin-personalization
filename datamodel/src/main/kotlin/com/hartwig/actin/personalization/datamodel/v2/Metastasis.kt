package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.Location

data class Metastasis(
    val daysSinceDiagnosis: Int?,
    val location: Location,
    val isLinkedToProgression: Boolean?
)
