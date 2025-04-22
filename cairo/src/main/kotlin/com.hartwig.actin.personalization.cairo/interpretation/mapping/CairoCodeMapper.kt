package com.hartwig.actin.personalization.cairo.interpretation.mapping

interface CairoIntCodeMapper<T> {
    fun resolve(code: Int): T

    fun resolve(code: Int?): T? {
        return code?.let(this::resolve)
    }
}

interface CairoStringCodeMapper<T> {
    fun resolve(code: String): T

    fun resolveNullable(code: String?): T? {
        return code?.let(this::resolve)
    }
}