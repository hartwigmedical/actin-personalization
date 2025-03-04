package com.hartwig.actin.personalization.datamodel.diagnosis

import kotlinx.serialization.Serializable

@Serializable
data class MetastaticDiagnosis(
    val distantMetastasesDetectionStatus: MetastasesDetectionStatus,
    val metastases: List<Metastasis>,
    val numberOfLiverMetastases: NumberOfLiverMetastases?,
    val maximumSizeOfLiverMetastasisMm: Int?,

    val investigatedLymphNodesCount: Int?,
    val positiveLymphNodesCount: Int?
)
