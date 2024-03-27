package com.hartwig.actin.personalization.ncr

import com.hartwig.actin.personalization.ncr.serialization.NCRDataReader
import com.hartwig.actin.personalization.ncr.serialization.datamodel.NCRRecord
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class NCRInspectionApplication {

    fun run() {
        LOGGER.info("Running NCR inspection application")

        val ncrDataPath = System.getProperty("user.home") + "/hmf/tmp/ncr_crc_dataset.csv"
        val ncrRecords = NCRDataReader.read(ncrDataPath)

        LOGGER.info(" Read {} NCR records from {}", ncrRecords.size, ncrDataPath)

        LOGGER.info("General statistics related to NCR database")
        LOGGER.info(" {} unique patients found, with {} tumors and {} episodes",
            patientCount(ncrRecords), tumorCount(ncrRecords), episodeCount(ncrRecords))
        LOGGER.info(" {} patients are still alive at the time of data release", alivePatients(ncrRecords))

        LOGGER.info("Done!")
    }

    private fun patientCount(ncrRecords: List<NCRRecord>): Int {
        return ncrRecords.map { it.identification.keyNkr }.distinct().count()
    }

    private fun tumorCount(ncrRecords: List<NCRRecord>): Int {
        return ncrRecords.map { it.identification.keyZid }.distinct().count()
    }

    private fun episodeCount(ncrRecords: List<NCRRecord>): Int {
        return ncrRecords.map { it.identification.keyEid }.distinct().count()
    }

    private fun alivePatients(ncrRecords: List<NCRRecord>): Any {
        return patientCount(ncrRecords.filter { it.patientCharacteristics.vitStat == 0 })
    }

    companion object {
        private val LOGGER: Logger = LogManager.getLogger(NCRInspectionApplication::class)
    }
}

fun main() {
    NCRInspectionApplication().run()
}

