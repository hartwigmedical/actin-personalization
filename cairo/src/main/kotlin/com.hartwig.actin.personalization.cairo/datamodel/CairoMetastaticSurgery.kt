package com.hartwig.actin.personalization.cairo.datamodel

import java.util.*

data class CairoMetastaticSurgery (
    val metastatesBiopsy: Boolean? = null,
    val metastasesBiopsyDate: Date? = null,
    val metastatesBiopsyPathology: String? = null,
    val metastasesBiopsyHospital: String? = null,

    val priorResectionMetastases: Boolean? = null,
    val priorResectionMetastasesDate: Date? = null,
    val priorResectionMetastasesSite: String? = null,
    val priorResectionMetastasesPathology: String? = null,
    val priorResectionMetastasesHospital: String? = null,
    )
