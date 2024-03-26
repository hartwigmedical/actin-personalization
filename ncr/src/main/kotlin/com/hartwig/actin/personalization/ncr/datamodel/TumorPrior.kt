package com.hartwig.actin.personalization.ncr.datamodel

data class TumorPrior(
    override val consolidatedTumorType: TumorType,
    override val consolidatedTumorLocation: TumorLocation,
    override val hasHadTumorDirectedSystemicTherapy: Boolean,

    val TumorPriorId: Int,
    val tumorLocationCategory: TumorLocationCategory,
    val stageTNM: StageTNM?,
    val treatments: List<Treatment> //to think about

): Tumor
