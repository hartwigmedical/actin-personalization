package com.hartwig.actin.personalization.ncr.serialization

data class NCRPrimaryRadiotherapy(
    val rt: Int,
    val rtType1: Int,
    val rtType2: Int,
    val rtStartInt1: Int,
    val rtStartInt2: Int,
    val rtStopInt1: Int,
    val rtStopInt2: Int,
    val rtDosis1: Double,
    val rtDosis2: Double
)