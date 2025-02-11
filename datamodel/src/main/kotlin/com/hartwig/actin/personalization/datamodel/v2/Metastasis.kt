package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.Location

data class Metastasis(
    val location: Location,
    val daysBetweenDiagnosisAndMetastasisDetection: Int?,
    val isLinkedToProgression: Boolean?
)
