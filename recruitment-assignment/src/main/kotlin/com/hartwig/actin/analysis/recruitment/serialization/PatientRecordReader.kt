package com.hartwig.actin.analysis.recruitment.serialization

import com.hartwig.actin.analysis.recruitment.datamodel.PatientRecord
import java.io.File
import java.nio.file.Files

object PatientRecordReader {

    private const val FIELD_DELIMITER = "\t"
    private const val VALUE_DELIMITER = ","

    fun read(tsv: String): List<PatientRecord> {
        val lines = Files.readAllLines(File(tsv).toPath())
        val fields = createFields(lines[0].split(FIELD_DELIMITER).dropLastWhile { it.isEmpty() }.toTypedArray())

        return lines.subList(1, lines.size).map { createRecord(fields, it.split(FIELD_DELIMITER).toTypedArray()) }
    }

    private fun createRecord(fields: Map<String, Int>, parts : Array<String>) : PatientRecord {
        return PatientRecord(
            id = parts[fields["id"]!!].toInt(),
            age = parts[fields["age"]!!].toInt(),
            ecog = parts[fields["ecog"]!!].toInt(),
            metastaticSites = toSet(parts[fields["metastaticSites"]!!]),
            pretreatedWithDocetaxel = parts[fields["pretreatedWithDocetaxel"]!!] == "Yes",
            psa = parts[fields["psa"]!!].toInt(),
            genesInactivated = toSet(parts[fields["genesInactivated"]!!]),
            treatmentChoice = parts[fields["treatmentChoice"]!!],
            pfs = parts[fields["pfs"]!!].toInt()
        )
    }

    private fun createFields(header: Array<String>): Map<String, Int> {
        val fields: MutableMap<String, Int> = HashMap()
        for ((i) in header.withIndex()) {
            fields[header[i]] = i
        }
        return fields
    }

    private fun toSet(string : String) : Set<String> {
        return string.split(VALUE_DELIMITER.toRegex()).toCollection(HashSet())
    }
}