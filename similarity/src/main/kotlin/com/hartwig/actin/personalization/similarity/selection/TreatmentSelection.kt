package com.hartwig.actin.personalization.similarity.selection

import com.hartwig.actin.personalization.datamodel.Tumor
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticPresence
import com.hartwig.actin.personalization.datamodel.treatment.SystemicTreatment
import com.hartwig.actin.personalization.datamodel.treatment.Treatment
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentEpisode

object TreatmentSelection {
    
    fun extractMetastaticTreatmentEpisode(tumor: Tumor): TreatmentEpisode? {
        return tumor.treatmentEpisodes.firstOrNull { it.metastaticPresence == MetastaticPresence.AT_START }
    }

    fun definedMetastaticSystemicTreatment(tumor: Tumor): SystemicTreatment? {
        return extractMetastaticTreatmentEpisode(tumor)?.let { extractDefinedSystemicTreatment(it) }
    }

    fun extractDefinedSystemicTreatment(treatmentEpisode: TreatmentEpisode): SystemicTreatment? {
        return treatmentEpisode.systemicTreatments.firstOrNull { it.treatment != Treatment.OTHER }
    }
}