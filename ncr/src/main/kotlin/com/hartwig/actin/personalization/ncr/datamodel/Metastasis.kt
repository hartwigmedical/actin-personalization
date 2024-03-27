package com.hartwig.actin.personalization.ncr.datamodel

data class Metastasis (
    val metastasisLocation: MetastasisLocation,
    val intervalTumorIncidenceMetastasisDetection: Int?, // TODO: meta_prog
)