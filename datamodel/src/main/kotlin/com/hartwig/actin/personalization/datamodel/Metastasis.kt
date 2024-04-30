package com.hartwig.actin.personalization.datamodel

data class Metastasis (
    val metastasisLocation: Location,
    val intervalTumorIncidenceMetastasisDetection: Int?,
    val progression: Boolean?
)