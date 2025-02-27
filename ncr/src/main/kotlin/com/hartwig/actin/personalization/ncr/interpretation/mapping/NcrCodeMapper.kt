package com.hartwig.actin.personalization.ncr.interpretation.mapping

interface NcrIntCodeMapper<T> {
    fun resolve(code: Int): T

    fun resolve(code: Int?): T? {
        return code?.let(this::resolve)
    }
}

interface NcrStringCodeMapper<T> {
    fun resolve(code: String): T

    fun resolveNullable(code: String?): T? {
        return code?.let(this::resolve)
    }
}