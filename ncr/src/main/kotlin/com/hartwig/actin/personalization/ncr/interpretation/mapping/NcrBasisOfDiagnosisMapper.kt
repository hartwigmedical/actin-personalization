package com.hartwig.actin.personalization.ncr.interpretation.mapping

import com.hartwig.actin.personalization.datamodel.diagnosis.BasisOfDiagnosis

object NcrBasisOfDiagnosisMapper : NcrIntCodeMapper<BasisOfDiagnosis> {

    override fun resolve(code: Int): BasisOfDiagnosis {
        return when (code) {
            1 -> BasisOfDiagnosis.CLINICAL_ONLY_INVESTIGATION
            2 -> BasisOfDiagnosis.CLINICAL_AND_DIAGNOSTIC_INVESTIGATION
            4 -> BasisOfDiagnosis.SPEC_BIOCHEMICAL_IMMUNOLOGICAL_LAB_INVESTIGATION
            5 -> BasisOfDiagnosis.CYTOLOGICAL_CONFIRMATION
            6 -> BasisOfDiagnosis.HISTOLOGICAL_CONFIRMATION_METASTASES
            7 -> BasisOfDiagnosis.HISTOLOGICAL_CONFIRMATION
            else -> throw IllegalArgumentException("Unknown TumorBasisOfDiagnosis code: $code")
        }
    }
}
