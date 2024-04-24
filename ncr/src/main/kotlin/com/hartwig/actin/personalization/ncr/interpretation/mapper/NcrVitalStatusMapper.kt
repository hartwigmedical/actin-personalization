package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.VitalStatus
import com.hartwig.actin.personalization.ncr.interpretation.NcrIntCodeMapper

object NcrVitalStatusMapper : NcrIntCodeMapper<VitalStatus> {

    override fun resolve(code: Int): VitalStatus {
        return when (code) {
            0 -> VitalStatus.ALIVE
            1 -> VitalStatus.DEAD
            else -> throw IllegalArgumentException("Unknown VitalStatus code: $code")
        }
    }
}
