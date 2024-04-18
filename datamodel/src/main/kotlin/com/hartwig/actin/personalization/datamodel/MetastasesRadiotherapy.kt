package com.hartwig.actin.personalization.datamodel

data class MetastasesRadiotherapy(
    val metastasesRadiotherapyType: MetastasesRadiotherapyType,
    val intervalTumorIncidenceRadiotherapyMetastasesStart: Int?,
    val intervalTumorIncidenceRadiotherapyMetastasesStop: Int?,
)