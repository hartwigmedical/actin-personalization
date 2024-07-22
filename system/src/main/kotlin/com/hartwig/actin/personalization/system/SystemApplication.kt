package com.hartwig.actin.personalization.system

import com.hartwig.actin.personalization.database.PersonalizationLoaderApplication
import com.hartwig.actin.personalization.similarity.PersonalizationReportWriterApplication
import io.github.oshai.kotlinlogging.KotlinLogging

object SystemApplication {
    val LOGGER = KotlinLogging.logger {}
    val VERSION: String = SystemApplication::class.java.getPackage().implementationVersion
}

fun main() {
    SystemApplication.LOGGER.info { "The following applications are available through ACTIN Personalization v${SystemApplication.VERSION}" }
    listOf(
        PersonalizationLoaderApplication::class,
        PersonalizationReportWriterApplication::class,
    ).forEach { applicationClass -> SystemApplication.LOGGER.info { " ${applicationClass.java}" } }
}