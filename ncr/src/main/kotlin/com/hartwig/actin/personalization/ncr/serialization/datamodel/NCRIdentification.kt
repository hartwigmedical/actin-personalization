package com.hartwig.actin.personalization.ncr.serialization.datamodel

data class NCRIdentification(
    val keyNkr: Int,
    val keyZid: Int,
    val keyEid: Int,
    val epis: String,
    val metaEpis: Int,
    val teller: Int
)
