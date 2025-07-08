package com.hartwig.actin.personalization.cairo.serialization

import io.github.oshai.kotlinlogging.KotlinLogging

data class CairoFieldExtractor(private val fields: MutableMap<String, String>) {

    fun mandatoryString(property: String): String {
        return value(property)
    }

    fun optionalString(property: String): String? {
        return value(property).ifBlank { null }
    }

    fun mandatoryInt(property: String): Int {
        try {
            return value(property).toInt()
        } catch (exception: NumberFormatException) {
            logEpisodeID(property)
            throw exception
        }
    }

    fun optionalInt(property: String): Int? {
        try {
            return value(property).ifBlank { null }?.toInt()
        } catch (exception: NumberFormatException) {
            logEpisodeID(property)
            throw exception
        }
    }

    fun optionalDouble(property: String): Double? {
        try {
            return value(property).ifBlank { null }?.toDouble()
        } catch (exception: NumberFormatException) {
            logEpisodeID(property)
            throw exception
        }
    }

    private fun value(property: String): String {
        return fields[property]!!
    }

    private fun logEpisodeID(property: String) {
        LOGGER.warn { "Could not parse property '$property' with value '${value(property)}' for patient ${value(PATIENT_ID)}" }
    }

    companion object {
        private const val PATIENT_ID: String = "patnr"
        private val LOGGER = KotlinLogging.logger {}
    }
}
