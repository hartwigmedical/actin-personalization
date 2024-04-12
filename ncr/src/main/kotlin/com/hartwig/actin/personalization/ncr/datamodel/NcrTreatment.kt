package com.hartwig.actin.personalization.ncr.datamodel

data class NcrTreatment(
    val deelnameStudie: Int?,
    val tumgerichtTher: Int?,
    val geenTherReden: Int?,
    val gastroenterologyResection: NcrGastroenterologyResection,
    val primarySurgery: NcrPrimarySurgery,
    val metastaticSurgery: NcrMetastaticSurgery,
    val primaryRadiotherapy: NcrPrimaryRadiotherapy,
    val metastaticRadiotherapy: NcrMetastaticRadiotherapy,
    val systemicTreatment: NcrSystemicTreatment,
    val hipec: NcrHipec
)
