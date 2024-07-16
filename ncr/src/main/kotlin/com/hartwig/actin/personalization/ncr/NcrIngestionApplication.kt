package com.hartwig.actin.personalization.ncr

import com.hartwig.actin.personalization.datamodel.serialization.PatientRecordJson
import com.hartwig.actin.personalization.ncr.interpretation.PatientRecordFactory
import com.hartwig.actin.personalization.ncr.serialization.NcrDataReader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import picocli.CommandLine
import java.util.concurrent.Callable

class NcrIngestionApplication : Callable<Int> {

    @CommandLine.Option(names = ["-ncr_file"], required = true)
    lateinit var ncrFile: String

    @CommandLine.Option(names = ["-output_file"], required = true)
    lateinit var outputFile: String

    override fun call(): Int {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("Reading NCR dataset from {}", ncrFile)
        val ncrRecords = NcrDataReader.read(ncrFile)
        val patientRecords = PatientRecordFactory.default().create(ncrRecords)
        LOGGER.info(" Created {} patient records from {} NCR records", patientRecords.size, ncrRecords.size)

        LOGGER.info("Writing serialized records to {}", outputFile)
        PatientRecordJson.write(patientRecords, outputFile)

        LOGGER.info("Done!")
        return 0
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(NcrIngestionApplication::class.java)
        const val APPLICATION = "NCR inspection application"
        val VERSION = NcrIngestionApplication::class.java.getPackage().implementationVersion
    }
}

fun main(args: Array<String>): Unit = kotlin.system.exitProcess(CommandLine(NcrIngestionApplication()).execute(*args))
