package com.hartwig.actin.personalization.datamodel

data class RadiotherapyMetastases (
    val radiotherapyMetastasesType: RadiotherapyMetastasesType,
    val intervalTumorIncidenceRadiotherapyMetastasesStart: Int?,
    val intervalTumorIncidenceRadiotherapyMetastasesStop: Int?,
)