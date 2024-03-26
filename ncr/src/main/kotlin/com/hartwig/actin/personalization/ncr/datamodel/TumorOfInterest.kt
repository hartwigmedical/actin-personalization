package com.hartwig.actin.personalization.ncr.datamodel

data class TumorOfInterest(
    override val tumorType: TumorType,
    override val tumorLocation: TumorLocation,
    override val tumorLocationCategory: TumorLocationCategory,
    override val hasHadTumorDirectedSystemicTherapy: Boolean,

    val id: Int,
    val ageAtIncidence: Int,
    val intervalIncidenceVitalStatus: Int,

    val hasHadPriorTumor: Boolean,
    val intervalIncidenceTumorPrior1: Int?, //Unsure, doesn't feel good with 1/2/3/4
    val intervalIncidenceTumorPrior2: Int?,
    val intervalIncidenceTumorPrior3: Int?,
    val intervalIncidenceTumorPrior4: Int?,

    ): Tumor
