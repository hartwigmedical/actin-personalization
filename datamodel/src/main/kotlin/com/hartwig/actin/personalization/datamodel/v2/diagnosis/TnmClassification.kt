package com.hartwig.actin.personalization.datamodel.v2.diagnosis

import kotlinx.serialization.Serializable

@Serializable
data class TnmClassification(
    val tumor: TnmT?,
    val lymphNodes: TnmN?,
    val metastasis: TnmM?
)
