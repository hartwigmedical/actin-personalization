package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.TumorEntry
import weka.core.Attribute

data class IntField(
    override val name: String,
    val function: (TumorEntry) -> Int?
) : Field {
    override fun getFor(entry: TumorEntry): Double? {
        return function.invoke(entry)?.toDouble()
    }

    override fun toAttribute(): Attribute {
        return Attribute(name)
    }
}
