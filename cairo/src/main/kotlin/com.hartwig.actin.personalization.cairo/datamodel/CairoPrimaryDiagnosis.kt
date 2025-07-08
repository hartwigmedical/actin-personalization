package com.hartwig.actin.personalization.cairo.datamodel

import java.util.*

data class CairoPrimaryDiagnosis(
    val dateDiagnosis: Date,
    val tnmClassification: Int? = null,

    val locationPrimaryTumor: Int,

    val sitePrimaryTumor: Int, //in C2: primaryTumorSite
    val sitePrimaryTumorInvolved: Int? = null,
    val sitePrimaryTumorMethod: Int? = null,
    val sitePrimaryTumorMeasurableLesion: Int? = null,
    val sitePrimaryTumorComments: String? = null
    )
