package com.hartwig.actin.personalization.ncr.serialization.datamodel

data class NCRTreatment(
    val deelnameStudie: Int?,
    val tumgerichtTher: Int?,
    val geenTherReden: Int?,
    val localResection: NCRLocalResection,
    val primarySurgery: NCRPrimarySurgery,
    val metastaticSurgery: NCRMetastaticSurgery,
    val primaryRadiotherapy: NCRPrimaryRadiotherapy,
    val metastaticRadiotherapy: NCRMetastaticRadiotherapy,
    val systemicTreatment: NCRSystemicTreatment,
    val hipec: NCRHIPEC
)
