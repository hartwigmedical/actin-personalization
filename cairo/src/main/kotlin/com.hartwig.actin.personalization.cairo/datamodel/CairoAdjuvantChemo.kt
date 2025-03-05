package com.hartwig.actin.personalization.cairo.datamodel

import java.util.*

data class CairoAdjuvantChemo(
    val adjuvantChemo: Boolean,
    val adjuvantChemoDrugs: String? = null,
    val adjuvantChemoDateStart: Date? = null,
    val adjuvantChemoDateEnd: Date? = null,
)
