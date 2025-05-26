package com.hartwig.actin.personalization.datamodel.assessment

import kotlinx.serialization.Serializable

@Serializable
data class MolecularResult(
    val daysSinceDiagnosis: Int,

    val hasMsi: Boolean?,
    val hasBrafMutation: Boolean?,
    val hasBrafV600EMutation: Boolean?,
    val hasRasMutation: Boolean?,
    val hasKrasG12CMutation: Boolean?,
)