package com.hartwig.actin.personalization.demo

import com.hartwig.actin.personalization.demo.algo.NearestNeighborModel
import com.hartwig.actin.personalization.demo.serialization.ReferencePatientReader
import io.github.oshai.kotlinlogging.KotlinLogging

class PatientLikeMeApplication {

    fun run() {
        LOGGER.info { "Running patient-like-me application for WEKA demo" }

        val patientDatabasePath = this::class.java.classLoader.getResource("database/patients.tsv")!!.path
        LOGGER.info { "Reading database from $patientDatabasePath" }

        val patients = ReferencePatientReader.read(patientDatabasePath)
        LOGGER.info { "Printing patient data" }
        for (patient in patients) {
            LOGGER.info { " $patient" }
        }

        LOGGER.info { "Running nearest-neighbour model" }
        NearestNeighborModel.run(patients)
    }

    companion object {
        val LOGGER = KotlinLogging.logger {}
    }
}

fun main() {
    PatientLikeMeApplication().run()
}
