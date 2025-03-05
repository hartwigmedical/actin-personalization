package com.hartwig.actin.personalization.cairo.datamodel

import java.util.*

data class CairoUrinalysisValues(
    val labUrinalysisSampleDate: Date? = null,
    val proteinDipstick: Int? = null,
    val proteinG24H: Double? = null,
    val proteinGL: Double? = null
    )
