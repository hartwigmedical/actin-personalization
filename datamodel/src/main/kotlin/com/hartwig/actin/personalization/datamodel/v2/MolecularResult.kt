package com.hartwig.actin.personalization.datamodel.v2

import kotlinx.serialization.Serializable

@Serializable
data class MolecularResult(
    val daysSinceDiagnosis: Int,

    val hasMsi: Boolean? = null,
    val hasBrafMutation: Boolean? = null,
    val hasBrafV600EMutation: Boolean? = null,
    val hasRasMutation: Boolean? = null,
    val hasKrasG12CMutation: Boolean? = null,
)