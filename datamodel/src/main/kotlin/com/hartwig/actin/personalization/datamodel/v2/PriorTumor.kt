package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.Drug
import com.hartwig.actin.personalization.datamodel.Location
import com.hartwig.actin.personalization.datamodel.TumorLocationCategory
import com.hartwig.actin.personalization.datamodel.TumorType

data class PriorTumor (
    val daysBeforeDiagnosis: Int?,
    val primaryTumorType: TumorType,
    val primaryTumorLocation: Location,
    val primaryTumorLocationCategory: TumorLocationCategory,
    val primaryTumorStage: TumorStage?,
    val systemicDrugsReceived: List<Drug>
)