package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.AsaClassification
import com.hartwig.actin.personalization.datamodel.MetastasesDetectionStatus
import com.hartwig.actin.personalization.datamodel.Metastasis
import com.hartwig.actin.personalization.datamodel.NumberOfLiverMetastases

data class MetastasisDiagnosis(
    val whoStatusPreTreatmentStart: Int? = null,
    val asaClassificationPreSurgeryOrEndoscopy: AsaClassification? = null,

    val investigatedLymphNodesCount: Int? = null,
    val positiveLymphNodesCount: Int? = null,

    val distantMetastasesDetectionStatus: MetastasesDetectionStatus,
    val metastases: List<Metastasis>,
    val numberOfLiverMetastases: NumberOfLiverMetastases? = null,
    val maximumSizeOfLiverMetastasisMm: Int? = null,
)
