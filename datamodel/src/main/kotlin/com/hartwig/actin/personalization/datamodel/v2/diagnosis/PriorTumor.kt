package com.hartwig.actin.personalization.datamodel.v2.diagnosis

import com.hartwig.actin.personalization.datamodel.v2.Drug
import kotlinx.serialization.Serializable

@Serializable
data class PriorTumor (
    val daysBeforeDiagnosis: Int?,
    val primaryTumorType: TumorType,
    val primaryTumorLocation: Location,
    val primaryTumorLocationCategory: TumorLocationCategory,
    val primaryTumorStage: TumorStage?,
    val systemicDrugsReceived: List<Drug>
)