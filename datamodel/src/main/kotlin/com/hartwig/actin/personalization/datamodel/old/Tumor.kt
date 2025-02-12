package com.hartwig.actin.personalization.datamodel.old

interface Tumor {
    val consolidatedTumorType: TumorType
    val tumorLocations: Set<Location>
    val hasHadTumorDirectedSystemicTherapy: Boolean
}