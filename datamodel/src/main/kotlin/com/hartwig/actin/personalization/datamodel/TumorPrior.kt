package com.hartwig.actin.personalization.datamodel

data class TumorPrior(
    override val consolidatedTumorType: TumorType,
    override val consolidatedTumorLocation: Location,
    override val hasHadTumorDirectedSystemicTherapy: Boolean,

    val tumorPriorId: Int,
    val tumorLocationCategory: TumorLocationCategory,
    val stageTNM: StageTNM?,
    val systemicTreatments: List<SystemicTreatment>

): Tumor
