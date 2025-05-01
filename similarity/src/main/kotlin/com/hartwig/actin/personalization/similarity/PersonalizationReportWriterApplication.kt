package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.diagnosis.LocationGroup
import com.hartwig.actin.personalization.similarity.population.MeasurementType
import com.hartwig.actin.personalization.similarity.report.ReportWriter
import com.hartwig.actin.personalization.similarity.report.TableContent
import io.github.oshai.kotlinlogging.KotlinLogging
import picocli.CommandLine
import java.util.concurrent.Callable

class PersonalizationReportWriterApplication : Callable<Int> {
    
    @CommandLine.Option(names = ["-reference_patients_json"], required = true)
    lateinit var referencePatientsJson: String

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
        LOGGER.info { "Running $APPLICATION v$VERSION" }

        val analysis = PersonalizedDataInterpreter.createFromFile(referencePatientsJson)
            .analyzePatient(age, whoStatus, hasRasMutation, extractTopLevelLocationGroups(metastasisLocationString))

        val measurementTables = listOf(
            MeasurementType.TREATMENT_DECISION,
            MeasurementType.PROGRESSION_FREE_SURVIVAL,
            MeasurementType.OVERALL_SURVIVAL
        ).map { TableContent.createForMeasurementType(analysis, it) }

        LOGGER.info { "Writing PDF report to $outputPath" }
        val writer = ReportWriter.create(outputPath)
        writer.writeReport("SOC personalized real-world evidence annotation", measurementTables, analysis.plots)

        LOGGER.info { "Done!" }
        return 0
    }

    private fun extractTopLevelLocationGroups(locationString: String): Set<LocationGroup> {
        return if (locationString.isEmpty()) emptySet() else {
            locationString.split(";").map { LocationGroup.valueOf(it).topLevelGroup() }.toSet()
        }
    }

    companion object {
        val LOGGER = KotlinLogging.logger {}
        const val APPLICATION = "ACTIN-Personalization Report Writer"
        val VERSION: String = PersonalizationReportWriterApplication::class.java.getPackage().implementationVersion
    }
}

fun main(args: Array<String>): Unit = kotlin.system.exitProcess(CommandLine(PersonalizationReportWriterApplication()).execute(*args))
