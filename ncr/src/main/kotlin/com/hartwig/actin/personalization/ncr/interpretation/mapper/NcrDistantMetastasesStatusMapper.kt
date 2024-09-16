package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.MetastasesDetectionStatus

object NcrDistantMetastasesStatusMapper : NcrIntCodeMapper<MetastasesDetectionStatus> {

    override fun resolve(code: Int): MetastasesDetectionStatus {
        return when (code) {
            0 -> MetastasesDetectionStatus.ABSENT
            1 -> MetastasesDetectionStatus.AT_START
            2 -> MetastasesDetectionStatus.AT_PROGRESSION
            else -> throw IllegalArgumentException("Unknown DistantMetastasesStatus code: $code")
        }
    }
}
