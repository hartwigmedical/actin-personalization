package com.hartwig.actin.personalization.ncr.interpretation.mapper

object NcrBooleanMapper : NcrIntCodeMapper<Boolean?> {

    override fun resolve(code: Int): Boolean? {
        return when (code) {
            0 -> false
            1 -> true
            9 -> null
            else -> throw IllegalArgumentException("Unknown Boolean code: $code")
        }
    }
}
