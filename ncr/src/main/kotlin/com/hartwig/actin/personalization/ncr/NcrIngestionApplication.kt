package com.hartwig.actin.personalization.ncr

import com.hartwig.actin.personalization.datamodel.serialization.ReferenceEntryJson
import com.hartwig.actin.personalization.ncr.interpretation.NcrReferenceEntryFactory
import com.hartwig.actin.personalization.ncr.interpretation.filter.NcrQualityFilter
import com.hartwig.actin.personalization.ncr.serialization.NcrDataReader
import io.github.oshai.kotlinlogging.KotlinLogging
import picocli.CommandLine
import java.util.concurrent.Callable

class NcrIngestionApplication : Callable<Int> {

    @CommandLine.Option(names = ["-ncr_file"], required = true)
    lateinit var ncrFile: String

    @CommandLine.Option(names = ["-output_file"], required = true)
    lateinit var outputFile: String

    @CommandLine.Option(names = ["-log_filtered_records"])
    var logFilteredRecords: Boolean = false
    
    override fun call(): Int {
        try {
            LOGGER.info { "Running $APPLICATION v$VERSION" }

            LOGGER.info { "Reading NCR dataset from $ncrFile" }
            val ncrRecords = NcrDataReader.read(ncrFile)
            LOGGER.info { " Read ${ncrRecords.size} records" }

            LOGGER.info { "Creating reference entries for NCR" }
            val referenceEntries = NcrReferenceEntryFactory(NcrQualityFilter(logFilteredRecords = logFilteredRecords)).create(ncrRecords)
            LOGGER.info { " Created ${referenceEntries.size} reference entry records NCR records" }

            LOGGER.info { "Writing serialized reference entries to $outputFile" }
            ReferenceEntryJson.write(referenceEntries, outputFile)

            LOGGER.info { "Done!" }
            return 0
        } catch (exception: Exception) {
            LOGGER.error(exception) { "Failed to ingest NCR dataset: $exception" }
            return 1
        }
    }

    companion object {
        val LOGGER = KotlinLogging.logger {}
        const val APPLICATION = "NCR ingestion application"
        val VERSION = NcrIngestionApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

fun main(args: Array<String>): Unit = kotlin.system.exitProcess(CommandLine(NcrIngestionApplication()).execute(*args))
