package com.hartwig.actin.personalization.database

import com.hartwig.actin.personalization.datamodel.Diagnosis
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.PatientRecord
import com.hartwig.actin.personalization.datamodel.TumorEntry

import org.apache.logging.log4j.LogManager
import org.jooq.DSLContext
import org.jooq.JSON
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.sql.DriverManager

private typealias IndexedList<T> = List<Pair<Int, T>>

class DatabaseAccess(private val context: DSLContext) {
    
    fun writeAllToDb(patientRecords: List<PatientRecord>) {
        clearAll()
        val indexedRecords = writePatientRecords(patientRecords)
        val tumorEntries = writeDiagnosisRecords(indexedRecords)
        val episodes = writeEpisodeRecords(tumorEntries)
    }
    
    private fun clearAll() {
        LOGGER.info(" Clearing all patient data")
        context.execute("SET FOREIGN_KEY_CHECKS = 0;")
        sequenceOf(
            Tables.DIAGNOSIS,
            Tables.DRUG,
            Tables.EPISODE,
            Tables.LABMEASUREMENT,
            Tables.LOCATION,
            Tables.PATIENTRECORD,
            Tables.PFSMEASURE,
            Tables.PRIORTUMOR,
            Tables.SURGERY,
            Tables.SYSTEMICTREATMENTCOMPONENT,
            Tables.SYSTEMICTREATMENTSCHEME
        ).forEach { context.truncate(it).execute() }
        context.execute("SET FOREIGN_KEY_CHECKS = 1;")
    }
    
    private fun writePatientRecords(patientRecords: List<PatientRecord>): IndexedList<PatientRecord> {
        LOGGER.info(" Writing patient records")
        val (indexedRecords, rows) = patientRecords.mapIndexed { index, record ->
            val patientId = index + 1
            val dbRecord = context.newRecord(Tables.PATIENTRECORD)
            dbRecord.from(record)
            dbRecord.set(Tables.PATIENTRECORD.ID, patientId)
            Pair(patientId, record) to dbRecord
        }.unzip()
        context.batchInsert(rows).execute()
        return indexedRecords
    }

    private fun writeDiagnosisRecords(patientRecords: IndexedList<PatientRecord>): IndexedList<TumorEntry> {
        LOGGER.info(" Writing diagnosis records")
        val (tumorEntries, rows) = patientRecords.flatMap { (patientId, patient) ->
            patient.tumorEntries.map { tumorEntry ->
                val dbRecord = context.newRecord(Tables.DIAGNOSIS)
                dbRecord.from(tumorEntry.diagnosis)
                dbRecord.set(Tables.DIAGNOSIS.PATIENTRECORDID, patientId)
                dbRecord.set(
                    Tables.DIAGNOSIS.TUMORLOCATIONS,
                    JSON.json(tumorEntry.diagnosis.tumorLocations.joinToString(",", prefix = "[", postfix = "]") { "\"$it\"" })
                )
                tumorEntry to dbRecord
            }
        }
            .mapIndexed { index, (tumorEntry, dbRecord) ->
                val diagnosisId = index + 1
                dbRecord.set(Tables.DIAGNOSIS.ID, diagnosisId)
                Pair(diagnosisId, tumorEntry) to dbRecord
            }
            .unzip()
        
        context.batchInsert(rows).execute()
        return tumorEntries
    }
    
    private fun writeEpisodeRecords(tumorEntries: IndexedList<TumorEntry>): IndexedList<Episode> {
        LOGGER.info(" Writing episode records")
        val (episodeRecords, rows) = tumorEntries.flatMap { (diagnosisId, tumorEntry) ->
            tumorEntry.episodes.map { episode ->
                val dbRecord = context.newRecord(Tables.EPISODE)
                dbRecord.from(episode)
                dbRecord.set(Tables.EPISODE.DIAGNOSISID, diagnosisId)
                dbRecord.set(
                    Tables.EPISODE.METASTASES,
                    JSON.json(episode.metastases.joinToString(",", prefix = "[", postfix = "]") { "\"${it.metastasisLocation.toString()}\"" })
                )
                episode to dbRecord
            }
        }
            .mapIndexed { index, (episode, dbRecord) ->
                val episodeId = index + 1
                dbRecord.set(Tables.EPISODE.ID, episodeId)
                Pair(episodeId, episode) to dbRecord
            }
            .unzip()
        
        context.batchInsert(rows).execute()
        return episodeRecords
    }

    companion object {
        private val LOGGER = LogManager.getLogger(DatabaseAccess::class.java)

        fun fromCredentials(user: String, pass: String, url: String): DatabaseAccess {
            // Disable annoying jooq self-ad messages
            System.setProperty("org.jooq.no-logo", "true")
            System.setProperty("org.jooq.no-tips", "true")
            val jdbcUrl = "jdbc:$url"
            val conn = DriverManager.getConnection(jdbcUrl, user, pass)
            val catalog = conn.catalog
            LOGGER.info("Connecting to database '{}'", catalog)
            val context = DSL.using(conn, SQLDialect.MYSQL)
            return DatabaseAccess(context)
        }
    }
}
