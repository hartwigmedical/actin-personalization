package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.diagnosis.MetastaticDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.PrimaryDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmClassification
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmM

class TnmInterpreter(private val primaryDiagnosis: PrimaryDiagnosis, private val metastaticDiagnosis: MetastaticDiagnosis) {

    private val metastaticTnmMSet = setOf(TnmM.M1, TnmM.M1A, TnmM.M1B, TnmM.M1C)

    fun clinicalTnm(): TnmClassification {
        return if (isMetastaticDiagnosisTnmPresentAndMetastatic()) {
            metastaticDiagnosis.clinicalTnmClassification!!
        } else {
            primaryDiagnosis.clinicalTnmClassification
        }
    }

    fun pathologicalTnm(): TnmClassification? {
        return if (isMetastaticDiagnosisTnmPresentAndMetastatic()) {
            metastaticDiagnosis.pathologicalTnmClassification
        } else {
            primaryDiagnosis.pathologicalTnmClassification
        }
    }
    
    private fun isMetastaticDiagnosisTnmPresentAndMetastatic() : Boolean {
        val hasMetastaticClinicalTnm = metastaticTnmMSet.contains(metastaticDiagnosis.clinicalTnmClassification?.tnmM)
        val hasMetastaticPathologicalTnm = metastaticTnmMSet.contains(metastaticDiagnosis.pathologicalTnmClassification?.tnmM)
        
        return metastaticDiagnosis.clinicalTnmClassification != null && (hasMetastaticClinicalTnm || hasMetastaticPathologicalTnm)
    }
}