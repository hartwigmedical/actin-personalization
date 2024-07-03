package com.hartwig.actin.personalization.ncr.datamodel

data class NcrPrimaryRadiotherapy(
    val rt: Int,
    val chemort: Int? = null,
    val rtType1: Int? = null,
    val rtType2: Int? = null,
    val rtStartInt1: Int? = null,
    val rtStartInt2: Int? = null,
    val rtStopInt1: Int? = null,
    val rtStopInt2: Int? = null,
    val rtDosis1: Double? = null,
    val rtDosis2: Double? = null
)