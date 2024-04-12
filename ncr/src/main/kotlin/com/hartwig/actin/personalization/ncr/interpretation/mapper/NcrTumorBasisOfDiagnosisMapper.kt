package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.TumorBasisOfDiagnosis

object NcrTumorBasisOfDiagnosisMapper : NcrCodeMapper<TumorBasisOfDiagnosis> {

    override fun resolve(code: Int): TumorBasisOfDiagnosis {
        return when (code) {
            1 -> TumorBasisOfDiagnosis.CLINICAL_ONLY_INVESTIGATION
            2 -> TumorBasisOfDiagnosis.CLINICAL_AND_DIAGNOSTIC_INVESTIGATION
            4 -> TumorBasisOfDiagnosis.SPEC_BIOCHEMICAL_IMMUNOLOGICAL_LAB_INVESTIGATION
            5 -> TumorBasisOfDiagnosis.CYTOLOGICAL_CONFIRMATION
            6 -> TumorBasisOfDiagnosis.HISTOLOGICAL_CONFIRMATION_METASTASES
            7 -> TumorBasisOfDiagnosis.HISTOLOGICAL_CONFIRMATION
            else -> throw IllegalArgumentException("Unknown TumorBasisOfDiagnosis code: $code")
        }
    }
}
