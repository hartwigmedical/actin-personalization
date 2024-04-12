package com.hartwig.actin.personalization.datamodel

data class TumorOfInterest(
    override val consolidatedTumorType: TumorType,
    override val consolidatedTumorSubLocation: TumorSubLocation,
    override val consolidatedTumorLocation: TumorLocation,
    override val hasHadTumorDirectedSystemicTherapy: Boolean,

    val hasHadPriorTumor: Boolean,
    val intervalsTumorIncidenceDiagnosisTumorPrior: List<Int?>

) : Tumor
