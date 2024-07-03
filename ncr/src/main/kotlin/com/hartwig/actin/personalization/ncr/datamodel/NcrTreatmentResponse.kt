package com.hartwig.actin.personalization.ncr.datamodel

data class NcrTreatmentResponse(
    val responsUitslag: String? = null,
    val responsInt: Int? = null,
    val pfsEvent1: Int? = null,
    val pfsEvent2: Int? = null,
    val pfsEvent3: Int? = null,
    val pfsEvent4: Int? = null,
    val fupEventType1: Int? = null,
    val fupEventType2: Int? = null,
    val fupEventType3: Int? = null,
    val fupEventType4: Int? = null,
    val pfsInt1: Int? = null,
    val pfsInt2: Int? = null,
    val pfsInt3: Int? = null,
    val pfsInt4: Int? = null
)