package com.hartwig.actin.personalization.similarity.weka

import com.hartwig.actin.personalization.datamodel.TumorEntry

data class DoubleField(
    override val name: String,
    val function: (TumorEntry) -> Double?
) : Field {
    override fun getFor(entry: TumorEntry): Double? = function(entry)
}
