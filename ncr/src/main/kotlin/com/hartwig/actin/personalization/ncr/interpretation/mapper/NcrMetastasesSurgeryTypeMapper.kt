package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.treatment.MetastasesSurgeryType

object NcrMetastasesSurgeryTypeMapper : NcrStringCodeMapper<MetastasesSurgeryType> {
    override fun resolve(code: String): MetastasesSurgeryType {
        return when (code) {
            "123C26M" -> MetastasesSurgeryType.METASTASECTOMY_OTHER_ABDOMINAL_ORGANS
            "123C38M" -> MetastasesSurgeryType.METASTASECTOMY_PLEURAL_METASTASES
            "123C41M" -> MetastasesSurgeryType.METASTASECTOMY_BONE
            "123C44M" -> MetastasesSurgeryType.METASTASECTOMY_SKIN
            "123C71M" -> MetastasesSurgeryType.METASTASECTOMY_BRAIN
            "123C422M" -> MetastasesSurgeryType.METASTASECTOMY_SPLEEN
            "123C481M" -> MetastasesSurgeryType.METASTASECTOMY_OMENTUM
            "123C482M" -> MetastasesSurgeryType.METASTASECTOMY_PERITONEUM
            "123C762M" -> MetastasesSurgeryType.METASTASECTOMY_ABDOMINAL_WALL
            "131C22M" -> MetastasesSurgeryType.WEDGE_EXCISION_SEGMENT_RESECTION_OF_THE_LIVER_DUE_TO_METASTASES
            "131C34M" -> MetastasesSurgeryType.WEDGE_EXCISION_OF_LUNG_DUE_TO_METASTASES
            "132C22M" -> MetastasesSurgeryType.LOBECTOMY_LIVER_DUE_TO_METASTASES
            "132C34M" -> MetastasesSurgeryType.SEGMENTAL_RESECTION_OF_LUNG_DUE_TO_METASTASES
            "133C22M" -> MetastasesSurgeryType.HEMIHEPATECTOMY_DUE_TO_METASTASES
            "133C34M" -> MetastasesSurgeryType.BISEGMENT_RESECTION_OF_LUNG_DUE_TO_METASTASES
            "134C22M" -> MetastasesSurgeryType.WEDGE_RESECTION_OF_THE_LIVER_DUE_TO_METASTASES
            "134C34M" -> MetastasesSurgeryType.LOBECTOMY_LUNG_DUE_TO_METASTASES
            "135C22M" -> MetastasesSurgeryType.BISEGMENT_OR_SEGMENT_RESECTION_OF_THE_LIVER_DUE_TO_METASTASES
            "135C34M" -> MetastasesSurgeryType.BILOBECTOMY_DUE_TO_METASTASES
            "140C34M" -> MetastasesSurgeryType.PNEUMONECTOMY_DUE_TO_METASTASES
            "140C56M" -> MetastasesSurgeryType.OVARIECTOMY_DUE_TO_METASTASES
            "310C26" -> MetastasesSurgeryType.CYTOREDUCTIVE_SURGERY
            "310C77" -> MetastasesSurgeryType.EXCISION_OF_NON_REGIONAL_LYMPH_NODE_METASTASES
            "310C481" -> MetastasesSurgeryType.OMENTECTOMY_DUE_TO_METASTASES
            "311C22" -> MetastasesSurgeryType.RADIOFREQUENCY_ABLATION_ON_LIVER_METASTASIS
            "312C22" -> MetastasesSurgeryType.METASTASECTOMY_LIVER_BY_MEANS_OF_NANOKNIFE_OR_IRE
            "314C34" -> MetastasesSurgeryType.CRYOABLATION_DUE_TO_LUNG_METASTASES
            "310000" -> MetastasesSurgeryType.METASTASECTOMY_NNO
            "311000" -> MetastasesSurgeryType.RADIOFREQUENCY_ABLATION_ON_METASTASIS
            "311001" -> MetastasesSurgeryType.MICROWAVE_ABLATION_ON_METASTASIS
            "311002" -> MetastasesSurgeryType.MICROWAVE_ABLATION_ON_LIVER_METASTASIS
            "312000" -> MetastasesSurgeryType.METASTASECTOMY_BY_MEANS_OF_NANOKNIFE_OR_IRE
            "314000" -> MetastasesSurgeryType.CRYOABLATION_DUE_TO_METASTASES
            "315000" -> MetastasesSurgeryType.LYMPH_NODE_DISSECTION_REGIONAL_LYMPH_NODE_METASTASES
            else -> throw IllegalArgumentException("Unknown MetastasesSurgeryType code: $code")
        }
    }
}