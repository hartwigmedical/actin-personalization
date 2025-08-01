package com.hartwig.actin.personalization.datamodel.diagnosis

import com.hartwig.actin.personalization.datamodel.treatment.Drug
import kotlinx.serialization.Serializable

@Serializable
data class PriorTumor (
    val daysBeforeDiagnosis: Int,
    val primaryTumorType: TumorType,
    val primaryTumorLocation: TumorLocation,
    val primaryTumorLocationCategory: TumorLocationCategory,
    val primaryTumorStage: TumorStage?,
    val systemicDrugsReceived: List<Drug>
)