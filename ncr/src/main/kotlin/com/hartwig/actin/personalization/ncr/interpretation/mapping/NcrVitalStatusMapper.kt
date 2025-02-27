package com.hartwig.actin.personalization.ncr.interpretation.mapping

object NcrVitalStatusMapper  : NcrIntCodeMapper<Boolean> {

    override fun resolve(code: Int): Boolean {
        return when (code) {
            0 -> true
            1 -> false
            else -> throw IllegalArgumentException("Unknown vital status code: $code")
        }
    }
}