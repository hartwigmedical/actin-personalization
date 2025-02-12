package com.hartwig.actin.personalization.datamodel.old

import com.hartwig.actin.personalization.datamodel.v2.Drug
import com.hartwig.actin.personalization.datamodel.v2.diagnosis.Location
import com.hartwig.actin.personalization.datamodel.v2.diagnosis.TumorLocationCategory
import com.hartwig.actin.personalization.datamodel.v2.diagnosis.TumorType
import kotlinx.serialization.Serializable

@Serializable
data class PriorTumor(
    override val consolidatedTumorType: TumorType,
    override val tumorLocations: Set<Location>,
    override val hasHadTumorDirectedSystemicTherapy: Boolean,

    val intervalTumorIncidencePriorTumorDays: Int?,
    val tumorPriorId: Int,
    val tumorLocationCategory: TumorLocationCategory,
    val stageTNM: StageTnm?,
    val systemicTreatments: List<Drug>
) : Tumor