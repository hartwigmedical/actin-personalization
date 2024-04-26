package com.hartwig.actin.personalization.datamodel

interface Tumor {
    val consolidatedTumorType: TumorType
    val tumorLocations: Set<Location>
    val hasHadTumorDirectedSystemicTherapy: Boolean
}