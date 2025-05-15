package com.hartwig.actin.personalization.ncr.interpretation.mapping

import com.hartwig.actin.personalization.datamodel.treatment.MetastaticSurgeryType

object NcrMetastaticSurgeryTypeMapper : NcrStringCodeMapper<MetastaticSurgeryType> {
    override fun resolve(code: String): MetastaticSurgeryType {
        return when (code) {
            "123C26M" -> MetastaticSurgeryType.METASTASECTOMY_OTHER_ABDOMINAL_ORGANS
            "123C38M" -> MetastaticSurgeryType.METASTASECTOMY_PLEURAL_METASTASES
            "123C41M" -> MetastaticSurgeryType.METASTASECTOMY_BONE
            "123C44M" -> MetastaticSurgeryType.METASTASECTOMY_SKIN
            "123C71M" -> MetastaticSurgeryType.METASTASECTOMY_BRAIN
            "123C422M" -> MetastaticSurgeryType.METASTASECTOMY_SPLEEN
            "123C481M" -> MetastaticSurgeryType.METASTASECTOMY_OMENTUM
            "123C482M" -> MetastaticSurgeryType.METASTASECTOMY_PERITONEUM
            "123C762M" -> MetastaticSurgeryType.METASTASECTOMY_ABDOMINAL_WALL
            "131C22M" -> MetastaticSurgeryType.WEDGE_EXCISION_SEGMENT_RESECTION_OF_THE_LIVER_DUE_TO_METASTASES
            "131C34M" -> MetastaticSurgeryType.WEDGE_EXCISION_OF_LUNG_DUE_TO_METASTASES
            "132C22M" -> MetastaticSurgeryType.LOBECTOMY_LIVER_DUE_TO_METASTASES
            "132C34M" -> MetastaticSurgeryType.SEGMENTAL_RESECTION_OF_LUNG_DUE_TO_METASTASES
            "133C22M" -> MetastaticSurgeryType.HEMIHEPATECTOMY_DUE_TO_METASTASES
            "133C34M" -> MetastaticSurgeryType.BISEGMENT_RESECTION_OF_LUNG_DUE_TO_METASTASES
            "134C22M" -> MetastaticSurgeryType.WEDGE_RESECTION_OF_THE_LIVER_DUE_TO_METASTASES
            "134C34M" -> MetastaticSurgeryType.LOBECTOMY_LUNG_DUE_TO_METASTASES
            "135C22M" -> MetastaticSurgeryType.BISEGMENT_OR_SEGMENT_RESECTION_OF_THE_LIVER_DUE_TO_METASTASES
            "135C34M" -> MetastaticSurgeryType.BILOBECTOMY_DUE_TO_METASTASES
            "140C34M" -> MetastaticSurgeryType.PNEUMONECTOMY_DUE_TO_METASTASES
            "140C56M" -> MetastaticSurgeryType.OVARIECTOMY_DUE_TO_METASTASES
            "310C26" -> MetastaticSurgeryType.CYTOREDUCTIVE_SURGERY
            "310C77" -> MetastaticSurgeryType.EXCISION_OF_NON_REGIONAL_LYMPH_NODE_METASTASES
            "310C481" -> MetastaticSurgeryType.OMENTECTOMY_DUE_TO_METASTASES
            "311C22" -> MetastaticSurgeryType.RADIOFREQUENCY_ABLATION_ON_LIVER_METASTASIS
            "312C22" -> MetastaticSurgeryType.METASTASECTOMY_LIVER_BY_MEANS_OF_NANOKNIFE_OR_IRE
            "314C34" -> MetastaticSurgeryType.CRYOABLATION_DUE_TO_LUNG_METASTASES
            "310000" -> MetastaticSurgeryType.METASTASECTOMY_NNO
            "311000" -> MetastaticSurgeryType.RADIOFREQUENCY_ABLATION_ON_METASTASIS
            "311001" -> MetastaticSurgeryType.MICROWAVE_ABLATION_ON_METASTASIS
            "311002" -> MetastaticSurgeryType.MICROWAVE_ABLATION_ON_LIVER_METASTASIS
            "312000" -> MetastaticSurgeryType.METASTASECTOMY_BY_MEANS_OF_NANOKNIFE_OR_IRE
            "314000" -> MetastaticSurgeryType.CRYOABLATION_DUE_TO_METASTASES
            "315000" -> MetastaticSurgeryType.LYMPH_NODE_DISSECTION_REGIONAL_LYMPH_NODE_METASTASES
            else -> throw IllegalArgumentException("Unknown MetastasesSurgeryType code: $code")
        }
    }
}