package com.hartwig.actin.personalization.cairo.datamodel

import java.util.*

data class CairoPrimaryDiagnosis(
    val dateDiagnosis: Date,
    val tnmClassification: Int? = null,

    val sitePrimaryTumor: Int,
    val sitePrimaryTumorInvolved: Int? = null,
    val sitePrimaryTumorMethod: Int? = null,
    val sitePrimaryTumorMeasurableLesion: Int? = null,
    val sitePrimaryTumorComments: String? = null
    )
