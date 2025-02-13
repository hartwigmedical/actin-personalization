package com.hartwig.actin.personalization.ncr

import com.hartwig.actin.personalization.datamodel.serialization.ReferencePatientJson
import com.hartwig.actin.personalization.ncr.interpretation.ReferencePatientFactory
import com.hartwig.actin.personalization.ncr.serialization.NcrDataReader
import io.github.oshai.kotlinlogging.KotlinLogging
import picocli.CommandLine
import java.util.concurrent.Callable

class NcrIngestionApplication : Callable<Int> {

    @CommandLine.Option(names = ["-ncr_file"], required = true)
    lateinit var ncrFile: String

    @CommandLine.Option(names = ["-output_file"], required = true)
    lateinit var outputFile: String

    override fun call(): Int {
        try {
            LOGGER.info { "Running $APPLICATION v$VERSION" }

            LOGGER.info { "Reading NCR dataset from $ncrFile" }
            val ncrRecords = NcrDataReader.read(ncrFile)
            val patientRecords = ReferencePatientFactory.default().create(ncrRecords)
            LOGGER.info { " Created ${patientRecords.size} patient records from ${ncrRecords.size} NCR records" }

            LOGGER.info { "Writing serialized records to $outputFile" }
            ReferencePatientJson.write(patientRecords, outputFile)

            LOGGER.info { "Done!" }
            return 0
        } catch (e: Exception) {
            LOGGER.error(e) { "Failed to ingest NCR dataset: $e" }
            return 1
        }
    }

    companion object {
        val LOGGER = KotlinLogging.logger {}
        const val APPLICATION = "NCR inspection application"
        val VERSION: String = NcrIngestionApplication::class.java.getPackage().implementationVersion
    }
}

fun main(args: Array<String>): Unit = kotlin.system.exitProcess(CommandLine(NcrIngestionApplication()).execute(*args))
