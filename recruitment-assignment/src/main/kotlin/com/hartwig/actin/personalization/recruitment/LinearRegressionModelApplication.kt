package com.hartwig.actin.personalization.recruitment

import com.hartwig.actin.personalization.recruitment.algo.LinearRegressionModel
import com.hartwig.actin.personalization.recruitment.serialization.ReferencePatientReader
import io.github.oshai.kotlinlogging.KotlinLogging

class LinearRegressionModelApplication {

    fun run() {
        LOGGER.info { "Running linear model application for recruitment assignment" }

        val patientDatabasePath = this::class.java.classLoader.getResource("database/patients.tsv")!!.path
        LOGGER.info { "Reading database from $patientDatabasePath" }

        val patients = ReferencePatientReader.read(patientDatabasePath)
        LOGGER.info { "Printing patient data" }
        for (patient in patients) {
            LOGGER.info { " $patient" }
        }

        LOGGER.info { "Running linear regression" }
        LinearRegressionModel.run(patients)
    }

    companion object {
        val LOGGER = KotlinLogging.logger {}
    }
}

fun main() {
    LinearRegressionModelApplication().run()
}