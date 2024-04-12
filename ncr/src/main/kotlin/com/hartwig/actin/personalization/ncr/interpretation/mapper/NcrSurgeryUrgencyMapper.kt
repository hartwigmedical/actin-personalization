package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.SurgeryUrgency

object NcrSurgeryUrgencyMapper : NcrCodeMapper<SurgeryUrgency?> {

    override fun resolve(code: Int): SurgeryUrgency? {
        return when (code) {
            0 -> SurgeryUrgency.ELECTIVE
            1 -> SurgeryUrgency.URGENT_LESS_THAN_TWELVE_HOURS_PLANNED
            2 -> SurgeryUrgency.PLACEMENT_STENT_OR_STOMA_LATER_FOLLOWED_BY_PLANNED_SURGERY
            3 -> SurgeryUrgency.URGENT_AT_LEAST_TWELVE_HOURS_BEFORE_PLANNED
            9 -> null
            else -> throw IllegalArgumentException("Unknown SurgeryUrgency code: $code")
        }
    }
}
