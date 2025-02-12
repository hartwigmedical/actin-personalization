package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.TnmM
import com.hartwig.actin.personalization.datamodel.TnmN
import com.hartwig.actin.personalization.datamodel.TnmT
import kotlinx.serialization.Serializable

@Serializable
data class TnmClassification(
    val tumor: TnmT?,
    val lymphNodes: TnmN?,
    val metastasis: TnmM?
)
