package com.hartwig.actin.personalization.cairo.datamodel

data class CairoLabValues(
    val HeamatologyValues: CairoHaematologyValues,
    val BiochemistryValues: CairoBiochemistryValues,
    val UrinalysisValues: CairoUrinalysisValues

    val labComments: String? = null

    )