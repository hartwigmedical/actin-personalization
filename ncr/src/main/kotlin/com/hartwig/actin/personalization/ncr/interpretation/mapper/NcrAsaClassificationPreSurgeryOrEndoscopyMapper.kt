package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.v2.assessment.AsaClassification

object NcrAsaClassificationPreSurgeryOrEndoscopyMapper : NcrIntCodeMapper<AsaClassification?> {

    override fun resolve(code: Int): AsaClassification? {
        return when (code) {
            1 -> AsaClassification.I
            2 -> AsaClassification.II
            3 -> AsaClassification.III
            4 -> AsaClassification.IV
            5 -> AsaClassification.V
            6 -> AsaClassification.VI
            9 -> null
            else -> throw IllegalArgumentException("Unknown AsaClassificationPreSurgeryOrEndoscopy code: $code")
        }
    }
}
