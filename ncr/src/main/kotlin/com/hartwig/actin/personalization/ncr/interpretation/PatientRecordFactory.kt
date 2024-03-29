package com.hartwig.actin.personalization.ncr.interpretation

import com.hartwig.actin.personalization.ncr.datamodel.PatientRecord
import com.hartwig.actin.personalization.ncr.serialization.datamodel.NCRRecord
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object PatientRecordFactory {

    fun create(ncrRecords : List<NCRRecord>) : List<PatientRecord> {
        return listOf()
    }

    private val LOGGER: Logger = LogManager.getLogger(PatientRecordFactory::class)
}