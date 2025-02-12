package com.hartwig.actin.personalization.datamodel.old

import com.hartwig.actin.personalization.datamodel.diagnosis.Location
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocationCategory
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorType
import com.hartwig.actin.personalization.datamodel.treatment.Drug
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