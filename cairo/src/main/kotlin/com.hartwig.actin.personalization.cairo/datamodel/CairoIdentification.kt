package com.hartwig.actin.personalization.cairo.datamodel

data class CairoIdentification(
    val patnr: Int,
    val subjectKey: Int?,
    val cairoStudy: Int //e.g. cairo 2 or 3
)
