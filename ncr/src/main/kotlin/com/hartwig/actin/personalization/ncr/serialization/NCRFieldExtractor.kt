package com.hartwig.actin.personalization.ncr.serialization

class NCRFieldExtractor(private val fields: Map<String, Int>, private val parts: Array<String>) {

    fun mandatoryString(property : String) : String {
        return value(property)
    }

    fun optionalString(property : String) : String? {
        return value(property).ifBlank { null }
    }

    fun mandatoryInt(property : String) : Int {
        return value(property).toInt()
    }

    fun optionalInt(property : String) : Int? {
        return value(property).ifBlank { null }?.toInt()
    }

    fun optionalDouble(property : String) : Double? {
        return value(property).ifBlank { null }?.toDouble()
    }

    fun hasValue(property : String) : Boolean {
        return value(property).isNotBlank()
    }

    private fun value(property : String) : String {
        return parts[fields[property]!!]
    }
}