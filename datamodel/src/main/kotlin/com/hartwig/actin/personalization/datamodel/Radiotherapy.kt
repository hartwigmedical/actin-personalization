package com.hartwig.actin.personalization.datamodel

data class Radiotherapy(
    val radiotherapyType: RadiotherapyType,
    val radiotherapyTotalDosage: Double,
    val intervalTumorIncidenceRadiotherapyStart: Int?,
    val intervalTumorIncidenceRadiotherapyStop: Int?,

    )
