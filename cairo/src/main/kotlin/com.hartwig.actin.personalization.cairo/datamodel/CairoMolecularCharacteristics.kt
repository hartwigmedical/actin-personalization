package com.hartwig.actin.personalization.cairo.datamodel

data class CairoMolecularCharacteristics(
    val BRAF: Int? = null,
    val BRAF_mutation: String? = null,
    val KRAS: Int? = null,
    val KRAS_mutation: String? = null,
    val NRAS: Int? = null,
    val NRAS_mutation: String? = null
)
