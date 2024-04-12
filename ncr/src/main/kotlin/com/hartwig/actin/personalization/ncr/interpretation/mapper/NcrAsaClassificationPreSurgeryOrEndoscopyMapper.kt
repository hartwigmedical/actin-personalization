package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.AsaClassificationPreSurgeryOrEndoscopy

object NcrAsaClassificationPreSurgeryOrEndoscopyMapper : NcrCodeMapper<AsaClassificationPreSurgeryOrEndoscopy?> {

    override fun resolve(code: Int): AsaClassificationPreSurgeryOrEndoscopy? {
        return when (code) {
            1 -> AsaClassificationPreSurgeryOrEndoscopy.I
            2 -> AsaClassificationPreSurgeryOrEndoscopy.II
            3 -> AsaClassificationPreSurgeryOrEndoscopy.III
            4 -> AsaClassificationPreSurgeryOrEndoscopy.IV
            5 -> AsaClassificationPreSurgeryOrEndoscopy.V
            6 -> AsaClassificationPreSurgeryOrEndoscopy.VI
            9 -> null
            else -> throw IllegalArgumentException("Unknown AsaClassificationPreSurgeryOrEndoscopy code: $code")
        }
    }
}
