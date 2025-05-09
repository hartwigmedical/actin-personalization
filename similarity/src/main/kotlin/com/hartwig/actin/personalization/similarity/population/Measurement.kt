package com.hartwig.actin.personalization.similarity.population

data class Measurement(
    val value: Double,
    val numEntries: Int,
    val min: Int? = null,
    val max: Int? = null,
    val iqr: Double? = null
)
