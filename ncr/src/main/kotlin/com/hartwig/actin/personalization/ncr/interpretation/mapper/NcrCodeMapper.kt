package com.hartwig.actin.personalization.ncr.interpretation.mapper

interface NcrCodeMapper<T> {

    fun resolve(code: Int): T
}