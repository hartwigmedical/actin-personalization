package com.hartwig.actin.personalization.datamodel

data class TumorOfInterest(
    override val consolidatedTumorType: TumorType,
    override val tumorLocations: Set<Location>,
    override val hasHadTumorDirectedSystemicTherapy: Boolean,

    val hasHadPriorTumor: Boolean,
    val priorTumors: List<PriorTumor>
) : Tumor
