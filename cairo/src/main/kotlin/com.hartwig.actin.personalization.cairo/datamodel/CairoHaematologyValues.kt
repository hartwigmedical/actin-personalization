package com.hartwig.actin.personalization.cairo.datamodel

import java.util.Date

data class CairoHaematologyValues (
    val labHaematologySampleDate: Date? = null,// how to interpret
    val WBC: Double? = null,
    val neutro: Double? = null,
    val platelets: Double? = null,
    val hemoglobin: Double? = null
)