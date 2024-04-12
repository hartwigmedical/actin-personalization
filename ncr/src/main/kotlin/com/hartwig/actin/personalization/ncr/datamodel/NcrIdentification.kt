package com.hartwig.actin.personalization.ncr.datamodel

data class NcrIdentification(
    val keyNkr: Int,
    val keyZid: Int,
    val keyEid: Int,
    val epis: String,
    val metaEpis: Int,
    val teller: Int
)
