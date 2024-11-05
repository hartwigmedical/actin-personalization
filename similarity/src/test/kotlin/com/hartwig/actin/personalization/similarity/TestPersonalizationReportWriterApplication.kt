package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.Treatment
import com.hartwig.actin.personalization.similarity.population.MeasurementType
import com.hartwig.actin.personalization.similarity.report.ReportWriter
import com.hartwig.actin.personalization.similarity.report.TableContent
import io.github.oshai.kotlinlogging.KotlinLogging

private val WORK_DIRECTORY = System.getProperty("user.dir")

class TestPersonalizationReportWriterApplication {

    fun run(): Int {
        LOGGER.info { "Running $APPLICATION v$VERSION" }

        val outputPath = "$WORK_DIRECTORY/out.pdf"
        val analysis = PersonalizedDataInterpreter.createFromReferencePatients(
            (1..1000 step 10)
                .map { patientWithTreatment(Treatment.FOLFOX, it) }
                .map { (diagnosis, episode) -> recordWithEpisode(diagnosis, episode) }
        )
            .analyzePatient(50, 1, false, emptySet())


        val measurementTables = listOf(
            MeasurementType.TREATMENT_DECISION, MeasurementType.PROGRESSION_FREE_SURVIVAL
        ).map { TableContent.createForMeasurementType(analysis, it) }

        LOGGER.info { "Writing PDF report to $outputPath" }
        val writer = ReportWriter.create(outputPath)
        writer.writeReport("SOC personalized real-world evidence annotation", measurementTables, analysis.plots)

        LOGGER.info { "Done!" }
        return 0
    }

    companion object {
        val LOGGER = KotlinLogging.logger {}
        const val APPLICATION = "ACTIN-Personalization Report Writer"
        val VERSION = PersonalizationReportWriterApplication::class.java.getPackage().implementationVersion
    }
}

fun main(): Unit =
    kotlin.system.exitProcess(TestPersonalizationReportWriterApplication().run())
