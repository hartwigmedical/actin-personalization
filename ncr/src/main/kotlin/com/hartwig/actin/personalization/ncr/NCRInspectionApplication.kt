package com.hartwig.actin.personalization.ncr

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class NCRInspectionApplication {

    fun run() {
        LOGGER.info("Running NCR inspection application")
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(NCRInspectionApplication::class)
    }
}

fun main() {
    NCRInspectionApplication().run()
}

