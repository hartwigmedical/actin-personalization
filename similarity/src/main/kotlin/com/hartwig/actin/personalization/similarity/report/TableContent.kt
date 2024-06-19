package com.hartwig.actin.personalization.similarity.report

import java.lang.IllegalArgumentException
import kotlin.collections.any

data class TableContent(val title: String, val headers: List<String>, val rows: List<List<String>>, val sizes: List<Float>? = null) {

    fun check() {
        if (sizes?.let { it.size == headers.size } == false) {
            throw IllegalArgumentException("Sizes must have the same number of elements as the headers")
        }
        if (rows.any { it.size != headers.size }) {
            throw IllegalArgumentException("All rows must have the same number of columns as the headers")
        }
    }
}
