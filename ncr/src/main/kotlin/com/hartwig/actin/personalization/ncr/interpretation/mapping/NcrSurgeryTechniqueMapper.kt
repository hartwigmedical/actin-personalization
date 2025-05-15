package com.hartwig.actin.personalization.ncr.interpretation.mapping

import com.hartwig.actin.personalization.datamodel.treatment.SurgeryTechnique

object NcrSurgeryTechniqueMapper : NcrIntCodeMapper<SurgeryTechnique?> {

    override fun resolve(code: Int): SurgeryTechnique? {
        return when (code) {
            0 -> SurgeryTechnique.OPEN
            1 -> SurgeryTechnique.CONVENTIONAL_SCOPIC_NO_CONVERSION
            2 -> SurgeryTechnique.CONVENTIONAL_SCOPIC_WITH_CONVERSION
            3 -> SurgeryTechnique.ROBOT_ASSISTED_NO_CONVERSION
            4 -> SurgeryTechnique.ROBOT_ASSISTED_WITH_CONVERSION
            9 -> null
            else -> throw IllegalArgumentException("Unknown SurgeryTechnique code: $code")
        }
    }
}
