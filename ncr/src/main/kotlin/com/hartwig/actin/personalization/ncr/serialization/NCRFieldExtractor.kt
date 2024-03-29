package com.hartwig.actin.personalization.ncr.serialization

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class NCRFieldExtractor(private val fields: Map<String, Int>, private val parts: Array<String>) {

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
        return parts[fields[property]!!]
    }

    private fun logEpisodeID(property: String) {
        LOGGER.warn("Could not parse property '{}' with value '{}' for episode {}", property, value(property), value(EPISODE_ID))
    }

    companion object {
        private const val EPISODE_ID: String = "key_eid"
        private val LOGGER: Logger = LogManager.getLogger(NCRFieldExtractor::class)
    }
}