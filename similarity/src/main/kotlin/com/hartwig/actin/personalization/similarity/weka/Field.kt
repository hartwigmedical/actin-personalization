package com.hartwig.actin.personalization.similarity.weka

import com.hartwig.actin.personalization.datamodel.old.TumorEntry
import weka.core.Attribute

interface Field {
    val name: String
    fun getFor(entry: TumorEntry): Double?

    fun toAttribute(): Attribute = Attribute(name)
}