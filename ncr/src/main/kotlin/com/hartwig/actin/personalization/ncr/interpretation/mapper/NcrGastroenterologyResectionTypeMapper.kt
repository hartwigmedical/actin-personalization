package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.GastroenterologyResectionType

object NcrGastroenterologyResectionTypeMapper : NcrCodeMapper<GastroenterologyResectionType> {

    override fun resolve(code: Int): GastroenterologyResectionType {
        return when (code) {
            1 -> GastroenterologyResectionType.ENDOSCOPIC_RESECTION_POLYPECTOMY
            2 -> GastroenterologyResectionType.ENDOMUCOSAL_RESECTION
            3 -> GastroenterologyResectionType.ENDOSUBMUCOSAL_DISSECTION
            4 -> GastroenterologyResectionType.ENDOSCOPIC_FULL_THICKNESS_RESECTION
            5 -> GastroenterologyResectionType.ENDOSCOPIC_INTERMUSCULAR_DISSECTION
            6 -> GastroenterologyResectionType.LOCAL_TUMOR_RESECTION_NOS
            7 -> GastroenterologyResectionType.LOCAL_TUMOR_EXCISION_NOS
            8 -> GastroenterologyResectionType.ABLATION
            9 -> GastroenterologyResectionType.LOCAL_TUMOR_DESTRUCTION
            else -> throw IllegalArgumentException("Unknown GastroenterologyResectionType code: $code")
        }
    }
}
