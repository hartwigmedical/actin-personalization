package com.hartwig.actin.personalization.datamodel.diagnosis

import kotlinx.serialization.Serializable

@Serializable
data class TnmClassification(
    val tnmT: TnmT?,
    val tnmN: TnmN?,
    val tnmM: TnmM?
)
