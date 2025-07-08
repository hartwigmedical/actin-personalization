package com.hartwig.actin.personalization.cairo

import com.hartwig.actin.personalization.cairo.interpretation.CairoReferencePatientFactory
import com.hartwig.actin.personalization.cairo.serialization.CairoDataReader
import io.github.oshai.kotlinlogging.KotlinLogging
import picocli.CommandLine
import java.util.concurrent.Callable

class NcrIngestionApplication : Callable<Int> {

    @CommandLine.Option(names = ["-cairo_folder"], required = true)
    lateinit var cairoFile: String

    @CommandLine.Option(names = ["-output_file"], required = true)
    lateinit var outputFile: String

    override fun call(): Int {
        try {
            LOGGER.info { "Running $APPLICATION v$VERSION" }

            LOGGER.info { "Reading Cairo dataset from $cairoFile" }
            val cairoRecords = CairoDataReader.read(cairoFile)
            val referencePatients = CairoReferencePatientFactory.create(cairoRecords)
            LOGGER.info { " Created ${referencePatients.size} reference patient records from ${cairoRecords.size} NCR records" }

            LOGGER.info { "Writing serialized reference patients to $outputFile" }
            CairoReferencePatientFactory.write(referencePatients, outputFile)

            LOGGER.info { "Done!" }
            return 0
        } catch (exception: Exception) {
            LOGGER.error(exception) { "Failed to ingest Cairo dataset: $exception" }
            return 1
        }
    }

    companion object {
        val LOGGER = KotlinLogging.logger {}
        const val APPLICATION = "Cairo ingestion application"
        val VERSION = NcrIngestionApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

fun main(args: Array<String>): Unit = kotlin.system.exitProcess(CommandLine(NcrIngestionApplication()).execute(*args))
