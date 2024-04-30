package com.hartwig.actin.personalization.ncr.datamodel

data class NcrPrimaryRadiotherapy(
    val rt: Int,
    val chemort: Int?,
    val rtType1: Int?,
    val rtType2: Int?,
    val rtStartInt1: Int?,
    val rtStartInt2: Int?,
    val rtStopInt1: Int?,
    val rtStopInt2: Int?,
    val rtDosis1: Double?,
    val rtDosis2: Double?
)