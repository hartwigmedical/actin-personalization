package com.hartwig.actin.personalization.ncr.datamodel

interface Tumor {
    val tumorType: TumorType
    val tumorLocation: TumorLocation
    val stageTNM: StageTNM
    val hasHadTumorDirectedSystemicTherapy: Boolean

}