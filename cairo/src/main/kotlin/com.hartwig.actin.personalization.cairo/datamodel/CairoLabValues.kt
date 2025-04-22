package com.hartwig.actin.personalization.cairo.datamodel

data class CairoLabValues(
    val HaematologyValues: CairoHaematologyValues,
    val BiochemistryValues: CairoBiochemistryValues,
    val UrinalysisValues: CairoUrinalysisValues,

    val labComments: String? = null

    )