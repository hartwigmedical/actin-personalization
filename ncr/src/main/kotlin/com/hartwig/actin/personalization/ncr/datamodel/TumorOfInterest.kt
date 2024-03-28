package com.hartwig.actin.personalization.ncr.datamodel

data class TumorOfInterest(
    override val consolidatedTumorType: TumorType,
    override val consolidatedTumorSubLocation: TumorSubLocation,
    override val consolidatedTumorLocation: TumorLocation,
    override val hasHadTumorDirectedSystemicTherapy: Boolean,

    val id: Int,

    val hasHadPriorTumor: Boolean,
    val intervalTumorIncidenceDiagnosisTumorPrior1: Int?, //Unsure, doesn't feel good with 1/2/3/4
    val intervalTumorIncidenceDiagnosisTumorPrior2: Int?,
    val intervalTumorIncidenceDiagnosisTumorPrior3: Int?,
    val intervalTumorIncidenceDiagnosisTumorPrior4: Int?,

    ): Tumor
