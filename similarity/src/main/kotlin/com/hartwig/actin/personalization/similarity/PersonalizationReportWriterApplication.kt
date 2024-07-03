package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.LocationGroup
import com.hartwig.actin.personalization.similarity.population.MeasurementType
import com.hartwig.actin.personalization.similarity.report.ReportWriter
import com.hartwig.actin.personalization.similarity.report.TableContent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import picocli.CommandLine
import java.util.concurrent.Callable

class PersonalizationReportWriterApplication : Callable<Int> {

    @CommandLine.Option(names = ["-ncr_file"], required = true)
    lateinit var ncrFile: String

    @CommandLine.Option(names = ["-age"], required = true)
    var age: Int = -1

    @CommandLine.Option(names = ["-who_status"], required = true)
    var whoStatus: Int = -1

    @CommandLine.Option(names = ["-has_ras_mutation"], required = true, arity = "1")
    var hasRasMutation: Boolean = false

    @CommandLine.Option(names = ["-metastasis_locations"], required = true)
    lateinit var metastasisLocationString: String

    @CommandLine.Option(names = ["-output_path"], required = true)
    lateinit var outputPath: String

    override fun call(): Int {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        val analysis = PersonalizedDataInterpreter.createFromFile(ncrFile)
            .analyzePatient(age, whoStatus, hasRasMutation, extractTopLevelLocationGroups(metastasisLocationString))
        
        val tables = listOf(MeasurementType.TREATMENT_DECISION, MeasurementType.PROGRESSION_FREE_SURVIVAL).map { measure ->
            TableContent.fromPersonalizedDataAnalysis(analysis, measure)
        }

        LOGGER.info("Writing PDF report to {}", outputPath)
        val writer = ReportWriter.create(outputPath)
        writer.writeReport("SOC personalized real-world evidence annotation", tables)

        LOGGER.info("Done!")
        return 0
    }

    private fun extractTopLevelLocationGroups(locationString: String): Set<LocationGroup> {
        return if (locationString.isEmpty()) emptySet() else {
            locationString.split(";").map { LocationGroup.valueOf(it).topLevelGroup() }.toSet()
        }
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(PersonalizationReportWriterApplication::class.java)
        const val APPLICATION = "ACTIN-Personalization Report Writer"
        val VERSION = PersonalizationReportWriterApplication::class.java.getPackage().implementationVersion
    }
}

fun main(args: Array<String>): Unit = kotlin.system.exitProcess(CommandLine(PersonalizationReportWriterApplication()).execute(*args))
