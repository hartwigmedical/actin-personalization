package com.hartwig.actin.personalization.similarity.selection

import com.hartwig.actin.personalization.datamodel.ReferenceEntry
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticPresence
import com.hartwig.actin.personalization.datamodel.treatment.SystemicTreatment
import com.hartwig.actin.personalization.datamodel.treatment.Treatment
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentEpisode

object TreatmentSelection {
    
    fun extractMetastaticTreatmentEpisode(entry: ReferenceEntry): TreatmentEpisode? {
        return entry.treatmentEpisodes.firstOrNull { it.metastaticPresence == MetastaticPresence.AT_START }
    }

    fun firstSpecificMetastaticSystemicTreatment(entry: ReferenceEntry): SystemicTreatment? {
        return extractMetastaticTreatmentEpisode(entry)?.let { extractFirstSpecificSystemicTreatment(it) }
    }

    fun extractFirstSpecificSystemicTreatment(treatmentEpisode: TreatmentEpisode): SystemicTreatment? {
        return treatmentEpisode.systemicTreatments.firstOrNull { it.treatment != Treatment.OTHER }
    }
}