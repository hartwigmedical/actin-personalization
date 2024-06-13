package com.hartwig.actin.personalization.datamodel

data class Metastasis (
    val location: Location,
    val intervalTumorIncidenceMetastasisDetection: Int?,
    val progression: Boolean?
)