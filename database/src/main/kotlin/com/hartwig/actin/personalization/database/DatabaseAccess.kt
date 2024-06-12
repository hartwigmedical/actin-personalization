package com.hartwig.actin.personalization.database

import com.hartwig.actin.personalization.datamodel.Diagnosis
import com.hartwig.actin.personalization.datamodel.Drug
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.GastroenterologyResection
import com.hartwig.actin.personalization.datamodel.Location
import com.hartwig.actin.personalization.datamodel.MetastasesRadiotherapy
import com.hartwig.actin.personalization.datamodel.MetastasesSurgery
import com.hartwig.actin.personalization.datamodel.Metastasis
import com.hartwig.actin.personalization.datamodel.PatientRecord
import com.hartwig.actin.personalization.datamodel.Radiotherapy
import com.hartwig.actin.personalization.datamodel.SystemicTreatmentScheme
import com.hartwig.actin.personalization.datamodel.TumorEntry

import org.apache.logging.log4j.LogManager
import org.jooq.DSLContext
import org.jooq.JSON
import org.jooq.Meta
import org.jooq.SQLDialect
import org.jooq.Table
import org.jooq.TableRecord
import org.jooq.impl.DSL
import java.sql.DriverManager

private typealias IndexedList<T> = List<Pair<Int, T>>

class DatabaseAccess(private val context: DSLContext, private val connection: java.sql.Connection) {
    
    fun writeAllToDb(patientRecords: List<PatientRecord>) {
        context.execute("SET FOREIGN_KEY_CHECKS = 0;")
        connection.setAutoCommit(false)
        clearAll()
        
        val indexedRecords = writePatientRecords(patientRecords)
        val tumorEntries = writeRecordsAndReturnIndexedList("diagnosis", indexedRecords, Tables.DIAGNOSIS, ::patientToDiagnoses)
        val episodes = writeRecordsAndReturnIndexedList("episode", tumorEntries, Tables.EPISODE, ::tumorEntryToEpisodes)
        writeRecords("prior tumor", tumorEntries, ::tumorEntryToPriorTumorRecords)
        writeRecords("lab measurement", episodes, ::episodeToLabMeasurementRecords)
        writeRecords("surgery", episodes, ::episodeToSurgeryRecords)
        
        val systemicTreatmentSchemes = writeRecordsAndReturnIndexedList(
            "systemic treatment scheme", episodes, Tables.SYSTEMICTREATMENTSCHEME, ::episodeToSystemicTreatmentSchemes
        )
        writeRecords("systemic treatment component", systemicTreatmentSchemes, ::schemeToSystemicTreatmentComponentRecords)
        writeRecords("PFS measure", systemicTreatmentSchemes, ::schemeToPfsMeasureRecords)
        
        writeDrugs()
        writeLocations()
        
        connection.setAutoCommit(true)
        context.execute("SET FOREIGN_KEY_CHECKS = 1;")
    }
    
    private fun clearAll() {
        LOGGER.info(" Clearing all patient data")
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
        connection.commit()
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
        
        insertRows(rows, "patient")
        return indexedRecords
    }

    private fun <T, U : TableRecord<*>, V> writeRecordsAndReturnIndexedList(
        name: String, indexedRecords: IndexedList<T>, table: Table<U>, recordMapper: (Int, T) -> List<Pair<V, U>>
    ): IndexedList<V> {
        LOGGER.info(" Writing $name records")
        val (outputEntries, rows) = indexedRecords.flatMap { (foreignKeyId, record) -> recordMapper(foreignKeyId, record) }
            .mapIndexed { index, (outputEntry: V, dbRecord: U) ->
                val id = index + 1
                dbRecord.set(table.field("id", Int::class.java), id)
                Pair(id, outputEntry) to dbRecord
            }
            .unzip()

        insertRows(rows, name)
        return outputEntries
    }

    private fun <T, U : TableRecord<*>> writeRecords(name: String, indexedRecords: IndexedList<T>, recordMapper: (Int, T) -> List<U>) {
        LOGGER.info(" Writing $name records")
        val rows = indexedRecords.flatMap { (foreignKeyId, record) -> recordMapper(foreignKeyId, record) }
        insertRows(rows, name)
    }
    
    private fun insertRows(rows: List<TableRecord<*>>, name: String) {
        context.batchInsert(rows).execute()
        connection.commit()
        LOGGER.info("  Inserted ${rows.size} $name records")
    }

    private fun patientToDiagnoses(patientId: Int, patient: PatientRecord) =
        patient.tumorEntries.map { tumorEntry ->
            val dbRecord = context.newRecord(Tables.DIAGNOSIS)
            dbRecord.from(tumorEntry.diagnosis)
            dbRecord.set(Tables.DIAGNOSIS.PATIENTRECORDID, patientId)
            dbRecord.set(Tables.DIAGNOSIS.TUMORLOCATIONS, jsonList(tumorEntry.diagnosis.tumorLocations))
            tumorEntry to dbRecord
        }

    
    private fun tumorEntryToEpisodes(diagnosisId: Int, tumorEntry: TumorEntry) =
        tumorEntry.episodes.map { episode ->
            val dbRecord = context.newRecord(Tables.EPISODE)
            dbRecord.from(episode)
            dbRecord.set(Tables.EPISODE.DIAGNOSISID, diagnosisId)
            dbRecord.set(Tables.EPISODE.METASTASES, jsonList(episode.metastases.map(Metastasis::metastasisLocation)))
            dbRecord.set(
                Tables.EPISODE.GASTROENTEROLOGYRESECTIONS,
                jsonList(episode.gastroenterologyResections.map(GastroenterologyResection::gastroenterologyResectionType))
            )
            dbRecord.set(
                Tables.EPISODE.METASTASESSURGERIES,
                jsonList(episode.metastasesSurgeries.map(MetastasesSurgery::metastasesSurgeryType))
            )
            dbRecord.set(Tables.EPISODE.RADIOTHERAPIES, jsonList(episode.radiotherapies.map(Radiotherapy::radiotherapyType)))
            dbRecord.set(
                Tables.EPISODE.METASTASESRADIOTHERAPIES,
                jsonList(episode.metastasesRadiotherapies.map(MetastasesRadiotherapy::metastasesRadiotherapyType))
            )
            episode to dbRecord
        }
    
    private fun tumorEntryToPriorTumorRecords(diagnosisId: Int, tumorEntry: TumorEntry) =
        tumorEntry.diagnosis.priorTumors.map { priorTumor ->
            val dbRecord = context.newRecord(Tables.PRIORTUMOR)
            dbRecord.from(priorTumor)
            dbRecord.set(Tables.PRIORTUMOR.DIAGNOSISID, diagnosisId)
            dbRecord.set(Tables.PRIORTUMOR.TUMORLOCATIONS, jsonList(priorTumor.tumorLocations))
            dbRecord.set(Tables.PRIORTUMOR.SYSTEMICTREATMENTS, jsonList(priorTumor.systemicTreatments))
            dbRecord
        }

    private fun episodeToLabMeasurementRecords(episodeId: Int, episode: Episode) =
        episode.labMeasurements.map { labMeasurement ->
            val dbRecord = context.newRecord(Tables.LABMEASUREMENT)
            dbRecord.from(labMeasurement)
            dbRecord.set(Tables.LABMEASUREMENT.EPISODEID, episodeId)
            dbRecord
        }
    
    private fun episodeToSurgeryRecords(episodeId: Int, episode: Episode) =
        episode.surgeries.map { surgery ->
            val dbRecord = context.newRecord(Tables.SURGERY)
            dbRecord.from(surgery)
            dbRecord.set(Tables.SURGERY.EPISODEID, episodeId)
            dbRecord
        }

    private fun episodeToSystemicTreatmentSchemes(episodeId: Int, episode: Episode) =
        episode.systemicTreatmentSchemes.map { scheme ->
            val dbRecord = context.newRecord(Tables.SYSTEMICTREATMENTSCHEME)
            dbRecord.from(scheme)
            dbRecord.set(Tables.SYSTEMICTREATMENTSCHEME.EPISODEID, episodeId)
            scheme to dbRecord
        }
    
    private fun schemeToSystemicTreatmentComponentRecords(schemeId: Int, scheme: SystemicTreatmentScheme) =
        scheme.treatmentComponents.map { component ->
            val dbRecord = context.newRecord(Tables.SYSTEMICTREATMENTCOMPONENT)
            dbRecord.from(component)
            dbRecord.set(Tables.SYSTEMICTREATMENTCOMPONENT.SYSTEMICTREATMENTSCHEMEID, schemeId)
            dbRecord
        }
    
    private fun schemeToPfsMeasureRecords(schemeId: Int, scheme: SystemicTreatmentScheme) =
        scheme.treatmentRawPfs.map { pfsMeasure ->
            val dbRecord = context.newRecord(Tables.PFSMEASURE)
            dbRecord.from(pfsMeasure)
            dbRecord.set(Tables.PFSMEASURE.SYSTEMICTREATMENTSCHEMEID, schemeId)
            dbRecord
        }

    private fun writeDrugs() {
        LOGGER.info(" Writing drug records")
        val rows = Drug.values().map { drug ->
            val dbRecord = context.newRecord(Tables.DRUG)
            dbRecord.set(Tables.DRUG.NAME, drug.toString())
            dbRecord.set(Tables.DRUG.TREATMENTCATEGORY, drug.category.toString())
            dbRecord
        }
        insertRows(rows, "drug")
    }

    private fun writeLocations() {
        LOGGER.info(" Writing location records")
        val rows = Location.values().map { location ->
            val dbRecord = context.newRecord(Tables.LOCATION)
            dbRecord.set(Tables.LOCATION.NAME, location.toString())
            dbRecord.set(Tables.LOCATION.GROUP, location.locationGroup.toString())
            dbRecord
        }
        insertRows(rows, "location")
    }

    private fun jsonList(items: Iterable<Any>): JSON {
        return JSON.json(items.joinToString(",", prefix = "[", postfix = "]") { "\"$it\"" })
    }

    companion object {
        private val LOGGER = LogManager.getLogger(DatabaseAccess::class.java)

        fun fromCredentials(user: String, pass: String, url: String): DatabaseAccess {
            // Disable annoying jooq self-ad messages
            System.setProperty("org.jooq.no-logo", "true")
            System.setProperty("org.jooq.no-tips", "true")
            val conn = DriverManager.getConnection("jdbc:$url", user, pass)
            
            LOGGER.info("Connecting to database '{}'", conn.catalog)
            val context = DSL.using(conn, SQLDialect.MYSQL)
            return DatabaseAccess(context, conn)
        }
    }
}
