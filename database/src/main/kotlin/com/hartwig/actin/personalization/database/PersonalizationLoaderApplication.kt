package com.hartwig.actin.personalization.database

import com.hartwig.actin.personalization.datamodel.serialization.ReferenceEntryJson
import io.github.oshai.kotlinlogging.KotlinLogging
import picocli.CommandLine
import java.util.concurrent.Callable

class PersonalizationLoaderApplication : Callable<Int> {

    @CommandLine.Option(names = ["-reference_entry_json"], required = true)
    lateinit var referenceEntryJson: String

    @CommandLine.Option(names = ["-db_user"], required = true)
    lateinit var dbUser: String

    @CommandLine.Option(names = ["-db_pass"], required = true)
    lateinit var dbPass: String

    @CommandLine.Option(names = ["-db_url"], required = true)
    lateinit var dbUrl: String

    override fun call(): Int {
        LOGGER.info { "Running $APPLICATION v$VERSION" }

        LOGGER.info { "Loading reference entries from file $referenceEntryJson" }
        val referenceEntries = ReferenceEntryJson.read(referenceEntryJson)
        LOGGER.info { " Loaded ${referenceEntries.size} reference entries" }

        val writer = DatabaseWriter.fromCredentials(dbUser, dbPass, dbUrl)
        LOGGER.info { "Writing ${referenceEntries.size} reference entries to database" }
        writer.writeAllToDb(referenceEntries)

        LOGGER.info { "Done!" }
        return 0
    }

    companion object {
        val LOGGER = KotlinLogging.logger {}
        const val APPLICATION = "ACTIN-Personalization Loader"
        private val VERSION = PersonalizationLoaderApplication::class.java.getPackage().implementationVersion
    }
}

fun main(args: Array<String>): Unit = kotlin.system.exitProcess(CommandLine(PersonalizationLoaderApplication()).execute(*args))
