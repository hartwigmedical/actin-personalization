package com.hartwig.actin.personalization.datamodel

interface Tumor {
    val consolidatedTumorType: TumorType
    val consolidatedTumorLocation: Location
    val hasHadTumorDirectedSystemicTherapy: Boolean
}