package com.hartwig.actin.personalization.ncr.serialization.datamodel

data class NCRPatientCharacteristics(
    val gesl: Int,
    val leeft: Int,
    val vitStat: Int?,
    val vitStatInt: Int?,
    val perfStat: Int?,
    val asa: Int?
)
