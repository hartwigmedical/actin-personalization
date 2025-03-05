package com.hartwig.actin.personalization.cairo.datamodel

import java.util.*

data class CairoTreatmentChanges(
    val doseReductionCapecitabine: Int? = null,
    val doseReductionCapecitabinePercentage: Double? = null,
    val doseReductionCapecitabineDateLast: Date? = null,

    val doseReductionOxaliplatin: Int? = null,
    val doseReductionOxaliplatinPercentage: Double? = null,
    val doseReductionOxaliplatinDateLast: Date? = null,

    val treatmentDelay: Int? = null,
    val treatmentDelayWeeks: Int? = null
)
