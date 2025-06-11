package com.hartwig.actin.personalization.datamodel.diagnosis

import kotlinx.serialization.Serializable

@Serializable
data class MetastaticDiagnosis(
    val isMetachronous: Boolean,
    val metastases: List<Metastasis>,
    val numberOfLiverMetastases: NumberOfLiverMetastases?,
    val maximumSizeOfLiverMetastasisMm: Int?,

    val clinicalTnmClassification: TnmClassification?,
    val pathologicalTnmClassification: TnmClassification?,

    val investigatedLymphNodesCount: Int?,
    val positiveLymphNodesCount: Int?
)
