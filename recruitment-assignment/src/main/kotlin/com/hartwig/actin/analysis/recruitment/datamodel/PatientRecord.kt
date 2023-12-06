package com.hartwig.actin.analysis.recruitment.datamodel

data class PatientRecord(
    val id : Int,
    val age : Int,
    val ecog : Int,
    val metastaticSites : Set<String>,
    val pretreatedWithDocetaxel: Boolean,
    val psa : Int,
    val genesInactivated : Set<String>,
    val treatmentChoice : String,
    val pfs : Int
)