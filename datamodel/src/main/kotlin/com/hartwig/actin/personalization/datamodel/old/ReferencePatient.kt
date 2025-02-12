package com.hartwig.actin.personalization.datamodel.old

import com.hartwig.actin.personalization.datamodel.v2.Sex
import kotlinx.serialization.Serializable

@Serializable
data class ReferencePatient(
    val ncrId: Int,
    val sex: Sex,
    val isAlive: Boolean,
    val tumorEntries: List<TumorEntry>
)