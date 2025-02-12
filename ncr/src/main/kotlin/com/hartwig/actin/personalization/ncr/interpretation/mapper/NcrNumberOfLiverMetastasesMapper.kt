package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.v2.diagnosis.NumberOfLiverMetastases

object NcrNumberOfLiverMetastasesMapper : NcrIntCodeMapper<NumberOfLiverMetastases?> {

    override fun resolve(code: Int): NumberOfLiverMetastases? {
        return when (code) {
            1 -> NumberOfLiverMetastases.ONE
            2 -> NumberOfLiverMetastases.TWO
            3 -> NumberOfLiverMetastases.THREE
            4 -> NumberOfLiverMetastases.FOUR
            5 -> NumberOfLiverMetastases.FIVE_OR_MORE
            7 -> NumberOfLiverMetastases.MULTIPLE_BUT_EXACT_NUMBER_UNKNOWN
            9 -> null
            else -> throw IllegalArgumentException("Unknown NumberOfLiverMetastases code: $code")
        }
    }
}
