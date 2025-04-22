package com.hartwig.actin.personalization.cairo.interpretation.extraction

import com.hartwig.actin.personalization.cairo.datamodel.CairoRecord
import com.hartwig.actin.personalization.datamodel.diagnosis.PrimaryDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocation

object CairoPrimaryDiagnosisExtractor {

    fun extract(records: List<CairoRecord>): PrimaryDiagnosis {
        val diagnosis = records.first().primaryDiagnosis

        return PrimaryDiagnosis(
            basisOfDiagnosis = diagnosis.sitePrimaryTumorMethod ,
            hasDoublePrimaryTumor = null,

            primaryTumorType = null,
            primaryTumorLocation = , //TODO: implement
            sidedness = null, //TODO: implement
            anorectalVergeDistanceCategory = null, //TODO: implement
            mesorectalFasciaIsClear = null, //TODO: implement
            distanceToMesorectalFasciaMm = null, //TODO: implement

            differentiationGrade = null, //TODO: implement
            clinicalTnmClassification = extractClinicalTnmClassification(diagnosis),
            pathologicalTnmClassification = extractPathologicalTnmClassification(diagnosis),
            clinicalTumorStage = , //TODO: implement
            pathologicalTumorStage = , //TODO: implement
            investigatedLymphNodesCount = null, //TODO: implement
            positiveLymphNodesCount = null, //TODO: implement

            presentedWithIleus = null, //TODO: implement
            presentedWithPerforation = null, //TODO: implement
            venousInvasionDescription = null, //TODO: implement
            lymphaticInvasionCategory = null, //TODO: implement
            extraMuralInvasionCategory = null, //TODO: implement
            tumorRegression = null //TODO: implement

        )
    }

    private fun extractPrimaryTumorLocation(sitePrimaryTumor: Int){
        return when(sitePrimaryTumor){
            1 -> TumorLocation
        }

    }
}