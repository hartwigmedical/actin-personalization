package com.hartwig.actin.personalization.datamodel.old

import com.hartwig.actin.personalization.datamodel.diagnosis.Location
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorType

interface Tumor {
    val consolidatedTumorType: TumorType
    val tumorLocations: Set<Location>
    val hasHadTumorDirectedSystemicTherapy: Boolean
}