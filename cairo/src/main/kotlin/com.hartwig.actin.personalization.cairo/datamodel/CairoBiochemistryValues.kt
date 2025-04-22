package com.hartwig.actin.personalization.cairo.datamodel

import java.util.*

data class CairoBiochemistryValues(
    val labBiochemistrySampleDate: Date? = null,
    val CRP: Double? = null,
    val alkalinePhosphatase: Double? = null,
    val bilirubin: Double? = null,
    val ASAT: Double? = null,
    val ALAT: Double? = null,
    val creatinin: Double? = null,
    val creatininClearance: Double? = null,
    val natrium: Double? = null,
    val kalium: Double? = null,
    val calcium: Double? = null,
    val phosphatase: Double? = null,
    val albumin: Double? = null,
    val ldh: Double? = null,
    val magnesium: Double? = null,
    val CEA: Double? = null
)
