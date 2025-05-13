package com.hartwig.actin.personalization.datamodel.serialization

import com.hartwig.actin.personalization.datamodel.ReferenceEntry
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object ReferenceEntryJson {

    fun fromJson(json: String): List<ReferenceEntry> = Json.decodeFromString(json)

    fun toJson(entries: List<ReferenceEntry>): String = Json.encodeToString(entries)

    fun read(file: String): List<ReferenceEntry> = fromJson(File(file).readText())

    fun write(entries: List<ReferenceEntry>, file: String) = File(file).writeText(toJson(entries))
}