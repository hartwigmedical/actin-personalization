package com.hartwig.actin.personalization.similarity.weka

import com.hartwig.actin.personalization.datamodel.old.TumorEntry

data class IntField(
    override val name: String,
    val function: (TumorEntry) -> Int?
) : Field {
    override fun getFor(entry: TumorEntry): Double? {
        return function(entry)?.toDouble()
    }
}
