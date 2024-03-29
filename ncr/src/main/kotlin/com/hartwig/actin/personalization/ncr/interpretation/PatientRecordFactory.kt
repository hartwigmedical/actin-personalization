package com.hartwig.actin.personalization.ncr.interpretation

import com.hartwig.actin.personalization.ncr.datamodel.PatientRecord
import com.hartwig.actin.personalization.ncr.datamodel.Sex
import com.hartwig.actin.personalization.ncr.datamodel.VitalStatus
import com.hartwig.actin.personalization.ncr.serialization.datamodel.NCRRecord

object PatientRecordFactory {

    fun create(ncrRecords: List<NCRRecord>): List<PatientRecord> {
        val recordsPerPatient: Map<Int, List<NCRRecord>> = ncrRecords.groupBy { it.identification.keyNkr }

        return recordsPerPatient.entries.map { createPatientRecord(it.value) }
    }

    private fun createPatientRecord(ncrRecords: List<NCRRecord>): PatientRecord {
        val patientIDs : List<Int> = ncrRecords.map { it.identification.keyNkr }.distinct()
        if (patientIDs.count() != 1) {
            throw IllegalStateException("Non-unique or missing patientID when creating a single patient record: $patientIDs")
        }

        return PatientRecord(
            id = patientIDs[0],
            sex = Sex.MALE,
            vitalStatus = VitalStatus.ALIVE,
            episodesPerTumorOfInterest = mapOf(),
            previousTumors = listOf()
        )
    }
}