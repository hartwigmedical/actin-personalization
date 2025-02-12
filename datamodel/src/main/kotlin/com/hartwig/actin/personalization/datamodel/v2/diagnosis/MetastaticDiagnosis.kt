package com.hartwig.actin.personalization.datamodel.v2.diagnosis

import kotlinx.serialization.Serializable

@Serializable
data class MetastaticDiagnosis(
    val distantMetastasesDetectionStatus: MetastasesDetectionStatus,
    val metastases: List<Metastasis>,
    val numberOfLiverMetastases: NumberOfLiverMetastases? = null,
    val maximumSizeOfLiverMetastasisMm: Int? = null,

    val investigatedLymphNodesCount: Int? = null,
    val positiveLymphNodesCount: Int? = null
)
