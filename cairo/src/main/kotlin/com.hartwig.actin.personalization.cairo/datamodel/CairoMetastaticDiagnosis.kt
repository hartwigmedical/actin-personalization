package com.hartwig.actin.personalization.cairo.datamodel

import java.util.*

data class CairoMetastaticDiagnosis(
    val dateFirstMetastasis: Date? = null,

    val baselineSumLongestDiameters: Int? = null,

    val lymphNodeMetastases: CairoMetastasesLymphNode?,
    val lungMetastases: CairoMetastasesLung?,
    val liverMetastases: CairoMetastasesLiver?,
    val skinMetastases: CairoMetastasesSkin?,
    val softTissueMetastases: CairoMetastasesSoftTissue?,
    val boneMetastases: CairoMetastasesBone?,
    val ascitisMetastases: CairoMetastasesAscitis?,
    val pleuralEffusionMetastases: CairoMetastasesPleuralEffusion?,
    val otherSiteMetastases: CairoMetastasesOther?
)
