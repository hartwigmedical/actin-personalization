package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.treatment.TreatmentEpisode
import com.hartwig.actin.personalization.selection.TreatmentSelection

class TreatmentInterpreter(private val treatmentEpisodes : List<TreatmentEpisode>) {

    private val metastaticTreatmentEpisode = TreatmentSelection.extractMetastaticTreatmentEpisode(treatmentEpisodes)
    
    fun hasMetastaticTreatmentEpisode() : Boolean {
        return metastaticTreatmentEpisode != null
    }
    
    fun determineSystemicTreatmentStartForMetastaticDisease() : Int? {
        return metastaticTreatmentEpisode?.systemicTreatments?.mapNotNull { it.daysBetweenDiagnosisAndStart }?.minOfOrNull { it }
    }
}