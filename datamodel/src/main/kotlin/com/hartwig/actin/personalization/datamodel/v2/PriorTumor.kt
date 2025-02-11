package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.Drug
import com.hartwig.actin.personalization.datamodel.Location
import com.hartwig.actin.personalization.datamodel.StageTnm
import com.hartwig.actin.personalization.datamodel.TumorLocationCategory
import com.hartwig.actin.personalization.datamodel.TumorType

data class PriorTumor (
    val daysBeforeDiagnosis: Int?,

    val primaryTumorType: TumorType,
    val primaryTumorLocation: Location,
    val primaryTumorLocationCategory: TumorLocationCategory,

    // TODO (KD): Consider renaming to TumorStage (since it contains no TNM details for prior tumors?)
    val stageTnm: StageTnm?,

    // TODO (KD): Consider renaming to drugs or "drugs received".
    val systemicTreatments: List<Drug>
)