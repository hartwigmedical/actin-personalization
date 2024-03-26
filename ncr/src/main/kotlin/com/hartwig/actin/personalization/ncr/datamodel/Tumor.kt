package com.hartwig.actin.personalization.ncr.datamodel

interface Tumor {
    val consolidatedTumorType: TumorType
    val consolidatedTumorLocation: TumorLocation
    val hasHadTumorDirectedSystemicTherapy: Boolean
}