package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.ncr.interpretation.NcrIntCodeMapper

object NcrBooleanMapper : NcrIntCodeMapper<Boolean?> {

    override fun resolve(code: Int): Boolean? {
        return when (code) {
            0 -> false
            1 -> true
            9 -> null
            else -> throw IllegalArgumentException("Unknown Boolean code: $code")
        }
    }

    fun resolve(code: Int?): Boolean? {
        return code?.let(this::resolve)
    }
}
