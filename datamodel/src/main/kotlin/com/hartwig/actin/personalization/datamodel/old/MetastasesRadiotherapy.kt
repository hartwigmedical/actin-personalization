package com.hartwig.actin.personalization.datamodel.old

import com.hartwig.actin.personalization.datamodel.treatment.MetastaticRadiotherapyType
import kotlinx.serialization.Serializable

@Serializable
data class MetastasesRadiotherapy(
    val metastasesRadiotherapyType: MetastaticRadiotherapyType,
    val intervalTumorIncidenceRadiotherapyMetastasesStartDays: Int?,
    val intervalTumorIncidenceRadiotherapyMetastasesStopDays: Int?,
)