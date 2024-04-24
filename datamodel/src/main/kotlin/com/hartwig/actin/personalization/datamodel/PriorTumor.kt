package com.hartwig.actin.personalization.datamodel

data class PriorTumor(
    override val consolidatedTumorType: TumorType,
    override val consolidatedTumorLocation: Location,
    override val hasHadTumorDirectedSystemicTherapy: Boolean,

    val incidenceIntervalPrimaryTumor: Int?,
    val tumorPriorId: Int,
    val tumorLocationCategory: TumorLocationCategory,
    val stageTNM: StageTnm?,
    val systemicTreatments: List<TreatmentName>

): Tumor
