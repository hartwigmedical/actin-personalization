package com.hartwig.actin.personalization.recruitment

import com.hartwig.actin.personalization.recruitment.algo.LinearRegressionModel
import com.hartwig.actin.personalization.recruitment.serialization.PatientRecordReader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class LinearRegressionModelApplication {

    fun run() {
        LOGGER.info("Running linear model application for recruitment assignment")

        val patientDatabasePath = this::class.java.classLoader.getResource("database/patients.tsv")!!.path
        LOGGER.info("Reading database from $patientDatabasePath")

        val patients = PatientRecordReader.read(patientDatabasePath)
        LOGGER.info("Printing patient data")
        for (patient in patients) {
            LOGGER.info(" $patient")
        }

        LOGGER.info("Running linear regression")
        LinearRegressionModel.run(patients)
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(LinearRegressionModelApplication::class)
    }
}

fun main() {
    LinearRegressionModelApplication().run()
}