package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.ncr.datamodel.DistantMetastasesStatus

object NcrDistantMetastasesStatusMapper : NcrCodeMapper<DistantMetastasesStatus> {

    override fun resolve(code: Int): DistantMetastasesStatus {
        return when (code) {
            0 -> DistantMetastasesStatus.ABSENT
            1 -> DistantMetastasesStatus.AT_START
            2 -> DistantMetastasesStatus.AT_PROGRESSION
            else -> throw IllegalArgumentException("Unknown DistantMetastasesStatus code: $code")
        }
    }
}
