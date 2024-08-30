package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class PriorTumor(
    override val consolidatedTumorType: TumorType,
    override val tumorLocations: Set<Location>,
    override val hasHadTumorDirectedSystemicTherapy: Boolean,

    val intervalTumorIncidencePriorTumorDays: Int?,
    val tumorPriorId: Int,
    val tumorLocationCategory: TumorLocationCategory,
    val stageTNM: StageTnm?,
    val systemicTreatments: List<Drug>
): Tumor
