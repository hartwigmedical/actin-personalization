package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.treatment.MetastasesRadiotherapyType

object NcrMetastasesRadiotherapyTypeMapper : NcrStringCodeMapper<MetastasesRadiotherapyType> {
    override fun resolve(code: String): MetastasesRadiotherapyType {
        return when (code) {
            "313C22" -> MetastasesRadiotherapyType.TRANSARTERIAL_RADIOEMBOLIZATION_FOR_LIVER_METASTASIS
            "320C22" -> MetastasesRadiotherapyType.RADIOTHERAPY_ON_LIVER_METASTASES
            "320C34" -> MetastasesRadiotherapyType.RADIOTHERAPY_ON_LUNG_METASTASES
            "320C38" -> MetastasesRadiotherapyType.RADIOTHERAPY_ON_PLEURAL_METASTASES
            "320C41" -> MetastasesRadiotherapyType.RADIOTHERAPY_ON_BONE_METASTASES
            "320C44" -> MetastasesRadiotherapyType.RADIOTHERAPY_ON_SKIN_METASTASES
            "320C70" -> MetastasesRadiotherapyType.RADIOTHERAPY_ON_LEPTOMENINGEAL_METASTASES
            "320C71" -> MetastasesRadiotherapyType.RADIOTHERAPY_ON_BRAIN_METASTASES_NNO
            "320C71_S" -> MetastasesRadiotherapyType.RADIOTHERAPY_ON_BRAIN_METASTASES_STEREOTACTIC_GAMMA_KNIFE_CYBER_KNIFE
            "320C71_W" -> MetastasesRadiotherapyType.RADIOTHERAPY_ON_BRAIN_METASTASES_WHOLE_BRAIN
            "320C77" -> MetastasesRadiotherapyType.RADIOTHERAPY_FOR_LYMPH_NODE_METASTASES
            "320000" -> MetastasesRadiotherapyType.RADIOTHERAPY_AIMED_AT_METASTASES_NNO
            "321000" -> MetastasesRadiotherapyType.PROPHYLACTIC_RADIOTHERAPY_TO_THE_CNS
            "322000" -> MetastasesRadiotherapyType.THERAPY_AIMED_AT_METASTASES_THROUGH_RADIOISOTOPES
            else -> throw IllegalArgumentException("Unknown RadiotherapyMetastasesType code: $code")
        }
    }
}