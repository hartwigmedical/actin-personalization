package com.hartwig.actin.personalization.ncr.interpretation.mapper

object NcrIntegerMapper : NcrIntCodeMapper<Int?> {

    override fun resolve(code: Int): Int? {
        return when (code) {
            9 -> null
            else -> code
        }
    }
}