package com.hartwig.actin.personalization.system

import com.hartwig.actin.personalization.database.PersonalizationLoaderApplication
import com.hartwig.actin.personalization.similarity.PersonalizationReportWriterApplication
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object SystemApplication {
    val LOGGER: Logger = LogManager.getLogger(SystemApplication::class.java)
    val VERSION: String = SystemApplication::class.java.getPackage().implementationVersion
}

fun main() {
    SystemApplication.LOGGER.info("The following applications are available through ACTIN Personalization v{}", SystemApplication.VERSION)
    listOf(
        PersonalizationLoaderApplication::class,
        PersonalizationReportWriterApplication::class,
    ).forEach { applicationClass -> SystemApplication.LOGGER.info(" {}", applicationClass.java) }
}