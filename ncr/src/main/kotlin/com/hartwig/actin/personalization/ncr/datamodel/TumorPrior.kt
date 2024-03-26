package com.hartwig.actin.personalization.ncr.datamodel

data class TumorPrior(
    override val tumorType: TumorType,
    override val tumorLocation: TumorLocation,
    override val tumorLocationCategory: TumorLocationCategory,
    override val hasHadTumorDirectedSystemicTherapy: Boolean,

    val TumorPriorId: Int,
    val stageTNM: StageTNM?,
    val treatments: List<Treatment> //to think about

): Tumor
