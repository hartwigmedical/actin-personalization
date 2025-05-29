package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.diagnosis.MetastaticDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.PrimaryDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmClassification
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmM

class TnmInterpreter(private val primaryDiagnosis: PrimaryDiagnosis, private val metastaticDiagnosis: MetastaticDiagnosis) {

    private val metastaticTnmMSet = setOf(TnmM.M1, TnmM.M1A, TnmM.M1B, TnmM.M1C)

    fun clinicalTnm(): TnmClassification {
        return metastaticDiagnosis.clinicalTnmClassification?.let { tnmClassification ->
            if (metastaticTnmMSet.contains(tnmClassification.tnmM)) tnmClassification else null
        } ?: primaryDiagnosis.clinicalTnmClassification
    }

    fun pathologicalTnm(): TnmClassification? {
        return metastaticDiagnosis.pathologicalTnmClassification?.let { tnmClassification ->
            if (metastaticTnmMSet.contains(tnmClassification.tnmM)) tnmClassification else null
        } ?: primaryDiagnosis.pathologicalTnmClassification
    }
}