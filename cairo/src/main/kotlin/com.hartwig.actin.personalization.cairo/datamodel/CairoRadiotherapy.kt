package com.hartwig.actin.personalization.cairo.datamodel

import java.util.*

data class CairoRadiotherapy(
    val radiotherapy: Boolean,
    val radiotherapySite: String? = null,
    val radiotherapyDateStart: Date? = null,
    val radiotherapyDateEnd: Date? = null,
    val radiotherapyDose: Double? = null,
)
