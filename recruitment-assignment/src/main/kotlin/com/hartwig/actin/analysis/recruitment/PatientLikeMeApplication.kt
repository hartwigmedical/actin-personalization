package com.hartwig.actin.analysis.recruitment

import com.hartwig.actin.analysis.recruitment.serialization.PatientRecordReader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class PatientLikeMeApplication {

    fun run() {
        LOGGER.info("Running patient-like-me application for recruitment assignment")

        val patientDatabase = this::class.java.classLoader.getResource("database/patients.tsv")!!.path
        LOGGER.info("Reading database from $patientDatabase")

        val patients = PatientRecordReader.read(patientDatabase)
        LOGGER.info("Printing patient data")
        for (patient in patients) {
            LOGGER.info(" $patient")
        }
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(PatientLikeMeApplication::class)
    }
}

fun main() {
    PatientLikeMeApplication().run()
}
