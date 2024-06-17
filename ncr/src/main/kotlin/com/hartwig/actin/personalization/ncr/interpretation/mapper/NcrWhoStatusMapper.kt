package com.hartwig.actin.personalization.ncr.interpretation.mapper

object NcrWhoStatusMapper : NcrIntCodeMapper<Int?> {

    override fun resolve(code: Int): Int? {
        return when (code) {
            0 -> 0
            1 -> 1
            2 -> 2
            3 -> 3
            4 -> 4
            5 -> 5
            9 -> null
            else -> throw IllegalArgumentException("Unknown WHO status code: $code")
        }
    }
}