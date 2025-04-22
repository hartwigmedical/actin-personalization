package com.hartwig.actin.personalization.cairo.datamodel

import java.util.Date

data class CairoPatientCharacteristics(
    val sex: Int,
    val age: Double,
    val whoStat: Int,
    val weight: Float?,
    val height: Float?,
    val birthdate: Date?,
    val smoking: Int?,

    val comorbidities: Boolean? = null,
    val comorbiditiesSpecification: String? = null
)