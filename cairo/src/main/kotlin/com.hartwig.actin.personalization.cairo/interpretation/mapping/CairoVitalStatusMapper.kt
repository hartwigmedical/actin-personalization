package com.hartwig.actin.personalization.cairo.interpretation.mapping

object CairoVitalStatusMapper: CairoIntCodeMapper<Boolean> {
    override fun resolve(code: Int): Boolean {
        return when (code) {
            0 -> true
            1 -> false
            else -> throw IllegalArgumentException("Unknown vital status code: $code")
        }
    }
}