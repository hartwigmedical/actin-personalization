package com.hartwig.actin.personalization.ncr.datamodel

data class TumorPrior(
    override val consolidatedTumorType: TumorType,
    override val consolidatedTumorSubLocation: TumorSubLocation,
    override val consolidatedTumorLocation: TumorLocation,
    override val hasHadTumorDirectedSystemicTherapy: Boolean,

    val tumorPriorId: Int,
    val tumorLocationCategory: TumorLocationCategory,
    val stageTNM: StageTNM?,
    val systemicTreatments: List<SystemicTreatment>

): Tumor
