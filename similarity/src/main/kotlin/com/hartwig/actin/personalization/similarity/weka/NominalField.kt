package com.hartwig.actin.personalization.similarity.weka

import com.hartwig.actin.personalization.datamodel.TumorEntry
import weka.core.Attribute

data class NominalField<T>(
    override val name: String,
    val values: List<T>,
    val function: (TumorEntry) -> T?
) : Field {
    private val stringValues = values.map { it.toString() }
    private val lookup = values.withIndex().associate { (i, v) -> v to i.toDouble() }

    override fun toAttribute(): Attribute = Attribute(name, stringValues)

    override fun getFor(entry: TumorEntry): Double? {
        return function.invoke(entry)?.let { lookup[it] }
    }

    companion object {
        fun <T : Enum<T>> enumField(enum: Class<T>, function: (TumorEntry) -> T?): NominalField<T> {
            return NominalField(enum.simpleName.replaceFirstChar(Char::lowercase), enum.enumConstants.toList(), function)
        }

        fun booleanField(name: String, function: (TumorEntry) -> Boolean?): NominalField<Boolean> {
            return NominalField(name, listOf(false, true), function)
        }
    }
}
