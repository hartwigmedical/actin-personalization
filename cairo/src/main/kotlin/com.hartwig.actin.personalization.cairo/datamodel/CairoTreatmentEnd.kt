package com.hartwig.actin.personalization.cairo.datamodel

import java.util.*

data class CairoTreatmentEnd(
    val dateLastDrugAdministation: Date? = null,

    val treatmentOxaplatinDateLast: Date? = null,
    val treatmentBevacizumabDateLast: Date? = null,
    val treatmentCapecitabineDateLast: Date? = null,
    )
