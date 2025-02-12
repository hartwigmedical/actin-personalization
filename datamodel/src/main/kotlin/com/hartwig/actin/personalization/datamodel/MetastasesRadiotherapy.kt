package com.hartwig.actin.personalization.datamodel

import com.hartwig.actin.personalization.datamodel.v2.treatment.MetastasesRadiotherapyType
import kotlinx.serialization.Serializable

@Serializable
data class MetastasesRadiotherapy(
    val metastasesRadiotherapyType: MetastasesRadiotherapyType,
    val intervalTumorIncidenceRadiotherapyMetastasesStartDays: Int?,
    val intervalTumorIncidenceRadiotherapyMetastasesStopDays: Int?,
)