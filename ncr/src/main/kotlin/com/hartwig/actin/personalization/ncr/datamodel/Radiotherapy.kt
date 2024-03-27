package com.hartwig.actin.personalization.ncr.datamodel

data class Radiotherapy(
    val radiotherapyType: RadiotherapyType,
    val radiotherapyTotalDosage: Double,
    val intervalTumorIncidenceRadiotherapyStart: Int?,
    val intervalTumorIncidenceRadiotherapyStop: Int?,

    )
