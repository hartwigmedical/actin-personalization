package com.hartwig.actin.personalization.ncr.datamodel

data class TumorOfInterest(
    override val tumorType: TumorType,
    override val tumorLocation: TumorLocation,
    override val stageTNM: StageTNM,
    override val hasHadTumorDirectedSystemicTherapy: Boolean,


    val id: Int,
    val ageAtIncidence: Int,
    val intervalIncidenceVitalStatus: Int,

    val hasHadPriorTumor: Boolean,
    val intervalPriorTumor1: Int?,
    val intervalPriorTumor2: Int?,
    val intervalPriorTumor3: Int?,
    val intervalPriorTumor4: Int?,

    ): Tumor
