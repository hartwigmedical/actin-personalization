package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.VitalStatus

object NcrVitalStatusMapper : NcrCodeMapper<VitalStatus> {

    override fun resolve(code: Int): VitalStatus {
        return when (code) {
            0 -> VitalStatus.ALIVE
            1 -> VitalStatus.DEAD
            else -> throw IllegalArgumentException("Unknown VitalStatus code: $code")
        }
    }
}
