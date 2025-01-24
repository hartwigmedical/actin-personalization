package com.hartwig.actin.personalization.datamodel.v2

data class MolecularResult(
    val hasMsi: Boolean? = null,
    val hasBrafMutation: Boolean? = null,
    val hasBrafV600EMutation: Boolean? = null,
    val hasRasMutation: Boolean? = null,
    val hasKrasG12CMutation: Boolean? = null,
)