package com.hartwig.actin.personalization.ncr.datamodel

data class TumorPrior(
    override val tumorType: TumorType,
    override val tumorLocation: TumorLocation,
    override val stageTNM: StageTNM,
    override val hasHadTumorDirectedSystemicTherapy: Boolean,

    val TumorPriorId: Int,

): Tumor
