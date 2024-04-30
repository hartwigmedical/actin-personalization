package com.hartwig.actin.personalization.ncr.datamodel

data class NcrPatientCharacteristics(
    val gesl: Int,
    val leeft: Int,
    val vitStat: Int?,
    val vitStatInt: Int?,
    val perfStat: Int?,
    val asa: Int?
)
