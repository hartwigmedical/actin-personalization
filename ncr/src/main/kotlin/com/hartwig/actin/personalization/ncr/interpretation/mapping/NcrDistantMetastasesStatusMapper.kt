package com.hartwig.actin.personalization.ncr.interpretation.mapping

import com.hartwig.actin.personalization.datamodel.diagnosis.MetastasesDetectionStatus

object NcrDistantMetastasesStatusMapper : NcrIntCodeMapper<MetastasesDetectionStatus> {

    override fun resolve(code: Int): MetastasesDetectionStatus {
        return when (code) {
            0 -> throw IllegalArgumentException("It is not expected to extract DistantMetastasesStatus code: 0")
            1 -> MetastasesDetectionStatus.AT_START
            2 -> MetastasesDetectionStatus.AT_PROGRESSION
            else -> throw IllegalArgumentException("Unknown DistantMetastasesStatus code: $code")
        }
    }
}
