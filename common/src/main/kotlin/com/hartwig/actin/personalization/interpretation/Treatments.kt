package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.treatment.TreatmentEpisode

object Treatments {

    fun determineSystemicTreatmentStart(treatmentEpisode: TreatmentEpisode) : Int? {
        return treatmentEpisode.systemicTreatments.mapNotNull { it.daysBetweenDiagnosisAndStart }.minOfOrNull { it }
    }
}