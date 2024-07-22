package com.hartwig.actin.personalization.datamodel.serialization

import com.hartwig.actin.personalization.datamodel.ReferencePatient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object ReferencePatientJson {

    fun fromJson(json: String): List<ReferencePatient> = Json.decodeFromString(json)

    fun toJson(patientRecords: List<ReferencePatient>): String = Json.encodeToString(patientRecords)

    fun read(file: String): List<ReferencePatient> = fromJson(File(file).readText())

    fun write(patientRecords: List<ReferencePatient>, file: String) = File(file).writeText(toJson(patientRecords))
}