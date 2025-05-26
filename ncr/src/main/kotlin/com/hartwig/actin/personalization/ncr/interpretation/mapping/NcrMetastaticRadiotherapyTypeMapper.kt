package com.hartwig.actin.personalization.ncr.interpretation.mapping

import com.hartwig.actin.personalization.datamodel.treatment.MetastaticRadiotherapyType

object NcrMetastaticRadiotherapyTypeMapper : NcrStringCodeMapper<MetastaticRadiotherapyType> {
    override fun resolve(code: String): MetastaticRadiotherapyType {
        return when (code) {
            "313C22" -> MetastaticRadiotherapyType.TRANSARTERIAL_RADIOEMBOLIZATION_FOR_LIVER_METASTASIS
            "320C22" -> MetastaticRadiotherapyType.RADIOTHERAPY_ON_LIVER_METASTASES
            "320C34" -> MetastaticRadiotherapyType.RADIOTHERAPY_ON_LUNG_METASTASES
            "320C38" -> MetastaticRadiotherapyType.RADIOTHERAPY_ON_PLEURAL_METASTASES
            "320C41" -> MetastaticRadiotherapyType.RADIOTHERAPY_ON_BONE_METASTASES
            "320C44" -> MetastaticRadiotherapyType.RADIOTHERAPY_ON_SKIN_METASTASES
            "320C70" -> MetastaticRadiotherapyType.RADIOTHERAPY_ON_LEPTOMENINGEAL_METASTASES
            "320C71" -> MetastaticRadiotherapyType.RADIOTHERAPY_ON_BRAIN_METASTASES_NNO
            "320C71_S" -> MetastaticRadiotherapyType.RADIOTHERAPY_ON_BRAIN_METASTASES_STEREOTACTIC_GAMMA_KNIFE_CYBER_KNIFE
            "320C71_W" -> MetastaticRadiotherapyType.RADIOTHERAPY_ON_BRAIN_METASTASES_WHOLE_BRAIN
            "320C77" -> MetastaticRadiotherapyType.RADIOTHERAPY_FOR_LYMPH_NODE_METASTASES
            "320000" -> MetastaticRadiotherapyType.RADIOTHERAPY_AIMED_AT_METASTASES_NNO
            "321000" -> MetastaticRadiotherapyType.PROPHYLACTIC_RADIOTHERAPY_TO_THE_CNS
            "322000" -> MetastaticRadiotherapyType.THERAPY_AIMED_AT_METASTASES_THROUGH_RADIOISOTOPES
            else -> throw IllegalArgumentException("Unknown RadiotherapyMetastasesType code: $code")
        }
    }
}