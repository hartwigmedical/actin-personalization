package com.hartwig.actin.personalization.database

import com.hartwig.actin.personalization.ncr.serialization.NcrDataReader
import com.hartwig.actin.personalization.ncr.interpretation.PatientRecordFactory
import picocli.CommandLine
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.system.exitProcess

class PersonalizationLoaderApplication {
    @CommandLine.Option(names = ["-ncr_file"], required = true)
    lateinit var ncrFile: String

    @CommandLine.Option(names = ["-db_user"], required = true)
    lateinit var dbUser: String

    @CommandLine.Option(names = ["-db_pass"], required = true)
    lateinit var dbPass: String

    @CommandLine.Option(names = ["-db_url"], required = true)
    lateinit var dbUrl: String

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("Loading NCR records from file $ncrFile")
        val records = NcrDataReader.read(ncrFile)
        LOGGER.info(" Loaded {} NCR records", records.size)

        LOGGER.info("Creating patient records")
        val patients = PatientRecordFactory.create(records)
        LOGGER.info(" Created {} patient records", patients.size)

        val access: DatabaseAccess = DatabaseAccess.fromCredentials(dbUser, dbPass, dbUrl)

        LOGGER.info("Writing {} patient records to database", patients.size)
        access.writePatientRecords(patients)

        LOGGER.info("Done!")
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(PersonalizationLoaderApplication::class.java)
        const val APPLICATION = "ACTIN-Personalization Loader"
        private val VERSION = PersonalizationLoaderApplication::class.java.getPackage().implementationVersion
    }
}

fun main(args: Array<String>): Unit = kotlin.system.exitProcess(CommandLine(PersonalizationLoaderApplication()).execute(*args))
