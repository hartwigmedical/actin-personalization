package com.hartwig.actin.analysis.recruitment

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class PatientLikeMeApplication {

    fun run() {
        LOGGER.info("Running patient-like-me application")
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(PatientLikeMeApplication::class)
    }
}

fun main(args: Array<String>) {
    PatientLikeMeApplication().run()
}
