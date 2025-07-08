package com.hartwig.actin.personalization.cairo.datamodel

import java.util.*

data class CairoPrimarySurgery(
    val resectionPrimaryTumor: Boolean? = null,
    val resectionPrimaryTumorDate: Date? = null,
    val resectionPrimaryTumorPathology: String? = null,
    val resectionPrimaryTumorHospital: String? = null,
    )
