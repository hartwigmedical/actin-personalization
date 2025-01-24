package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.Drug
import com.hartwig.actin.personalization.datamodel.Location
import com.hartwig.actin.personalization.datamodel.StageTnm
import com.hartwig.actin.personalization.datamodel.TumorLocationCategory
import com.hartwig.actin.personalization.datamodel.TumorType

data class PriorTumor (
    val daysBetweenDiagnosisAndPriorTumor: Int?,
    val consolidatedTumorType: TumorType,
    val tumorLocations: Set<Location>,
    val tumorLocationCategory: TumorLocationCategory,
    val stageTnm: StageTnm?,
    val systemicTreatments: List<Drug>
)