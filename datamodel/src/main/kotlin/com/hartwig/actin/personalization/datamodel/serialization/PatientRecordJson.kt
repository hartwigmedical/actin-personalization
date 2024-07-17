package com.hartwig.actin.personalization.datamodel.serialization

import com.hartwig.actin.personalization.datamodel.PatientRecord
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object PatientRecordJson {

    fun fromJson(json: String): List<PatientRecord> = Json.decodeFromString(json)

    fun toJson(patientRecords: List<PatientRecord>): String = Json.encodeToString(patientRecords)

    fun read(file: String): List<PatientRecord> = fromJson(File(file).readText())

    fun write(patientRecords: List<PatientRecord>, file: String) = File(file).writeText(toJson(patientRecords))
}