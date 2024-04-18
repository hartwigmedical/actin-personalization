package com.hartwig.actin.personalization.datamodel

data class TumorOfInterest(
    override val consolidatedTumorType: TumorType,
    override val consolidatedTumorLocation: Location,
    override val hasHadTumorDirectedSystemicTherapy: Boolean,

    val hasHadPriorTumor: Boolean,
    val intervalsTumorIncidenceDiagnosisTumorPrior: List<Int?>

) : Tumor
