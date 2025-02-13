package com.hartwig.actin.personalization.datamodel.old

import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocation
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorType

interface Tumor {
    val consolidatedTumorType: TumorType
    val tumorLocations: Set<TumorLocation>
    val hasHadTumorDirectedSystemicTherapy: Boolean
}