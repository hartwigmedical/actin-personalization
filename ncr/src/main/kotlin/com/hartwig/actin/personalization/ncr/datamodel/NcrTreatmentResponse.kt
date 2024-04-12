package com.hartwig.actin.personalization.ncr.datamodel

data class NcrTreatmentResponse(
    val responsUitslag: String?,
    val responsInt: Int?,
    val pfsEvent1: Int?,
    val pfsEvent2: Int?,
    val pfsEvent3: Int?,
    val pfsEvent4: Int?,
    val fupEventType1: Int?,
    val fupEventType2: Int?,
    val fupEventType3: Int?,
    val fupEventType4: Int?,
    val pfsInt1: Int?,
    val pfsInt2: Int?,
    val pfsInt3: Int?,
    val pfsInt4: Int?
)
