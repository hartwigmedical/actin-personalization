package com.hartwig.actin.personalization.ncr.datamodel

data class NcrPrimaryDiagnosis(
    val incjr: Int,
    val topoSublok: String,
    val morfCat: Int?,
    val diagBasis: Int,
    val diffgrad: String,
    val ct: String,
    val cn: String,
    val cm: String,
    val pt: String?,
    val pn: String?,
    val pm: String?,
    val cstadium: String?,
    val pstadium: String?,
    val stadium: String?,
    val ondLymf: Int?,
    val posLymf: Int?
)
