package com.hartwig.actin.personalization.ncr

import com.hartwig.actin.personalization.ncr.serialization.NCRDataReader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class NCRInspectionApplication {

    fun run() {
        LOGGER.info("Running NCR inspection application")

        val ncrDataPath = System.getProperty("user.home") + "/hmf/tmp/ncr_crc_dataset.csv"
        val ncrRecords = NCRDataReader.read(ncrDataPath)

        LOGGER.info(" Read {} NCR records from {}", ncrRecords.size, ncrDataPath)
    }

    companion object {
        private val LOGGER: Logger = LogManager.getLogger(NCRInspectionApplication::class)
    }
}

fun main() {
    NCRInspectionApplication().run()
}

