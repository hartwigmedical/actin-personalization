package com.hartwig.actin.personalization.cairo.interpretation.extraction

import com.hartwig.actin.personalization.cairo.datamodel.CairoRecord
import com.hartwig.actin.personalization.datamodel.diagnosis.PrimaryDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocation
import com.hartwig.actin.personalization.datamodel.diagnosis.Sidedness

object CairoPrimaryDiagnosisExtractor {

    fun extract(records: List<CairoRecord>): PrimaryDiagnosis {
        val diagnosis = records.first().primaryDiagnosis

        return PrimaryDiagnosis(
            basisOfDiagnosis = diagnosis.sitePrimaryTumorMethod,
            hasDoublePrimaryTumor = null,

            primaryTumorType = null,
            primaryTumorLocation = diagnosis.locationPrimaryTumor,
            sidedness = determineSidedness(primaryTumorLocation),
            anorectalVergeDistanceCategory = null,
            mesorectalFasciaIsClear = null,
            distanceToMesorectalFasciaMm = null,

            differentiationGrade = null,
            clinicalTnmClassification = ,//TODO: implement
            pathologicalTnmClassification = ,//TODO: implement
            clinicalTumorStage = null,
            pathologicalTumorStage = null,
            investigatedLymphNodesCount = null, //TODO: implement
            positiveLymphNodesCount = null, //TODO: implement

            presentedWithIleus = null,
            presentedWithPerforation = null,
            venousInvasionDescription = null,
            lymphaticInvasionCategory = null,
            extraMuralInvasionCategory = null,
            tumorRegression = null

        )
    }

    private fun determineSidedness(primaryTumorLocation: Int){
        return when(primaryTumorLocation){
            1 -> Sidedness.RIGHT
            2, 3, 4 -> Sidedness.LEFT
           else -> null
        }

    }
}