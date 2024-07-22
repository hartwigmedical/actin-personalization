package com.hartwig.actin.personalization.database

import com.hartwig.actin.personalization.datamodel.Drug
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.GastroenterologyResection
import com.hartwig.actin.personalization.datamodel.Location
import com.hartwig.actin.personalization.datamodel.LocationGroup
import com.hartwig.actin.personalization.datamodel.MetastasesRadiotherapy
import com.hartwig.actin.personalization.datamodel.MetastasesSurgery
import com.hartwig.actin.personalization.datamodel.ReferencePatient
import com.hartwig.actin.personalization.datamodel.Radiotherapy
import com.hartwig.actin.personalization.datamodel.SystemicTreatmentScheme
import com.hartwig.actin.personalization.datamodel.TumorEntry
import io.github.oshai.kotlinlogging.KotlinLogging

import org.jooq.DSLContext
import org.jooq.JSON
import org.jooq.SQLDialect
import org.jooq.Table
import org.jooq.TableRecord
import org.jooq.impl.DSL
import java.sql.DriverManager

private typealias IndexedList<T> = List<Pair<Int, T>>

class DatabaseWriter(private val context: DSLContext, private val connection: java.sql.Connection) {

    fun writeAllToDb(patientRecords: List<ReferencePatient>) {
        context.execute("SET FOREIGN_KEY_CHECKS = 0;")
        connection.setAutoCommit(false)
        clearAll()

        val indexedRecords = writeReferencePatients(patientRecords)
        val tumorEntries = writeRecordsAndReturnIndexedList("diagnosis", indexedRecords, Tables.DIAGNOSIS, ::diagnosesFromPatient)
        val episodes = writeRecordsAndReturnIndexedList("episode", tumorEntries, Tables.EPISODE, ::episodesFromTumorEntry)
        writeRecords("prior tumor", tumorEntries, ::priorTumorRecordsFromTumorEntry)
        writeRecords("metastasis", episodes, ::metastasisRecordsFromEpisode)
        writeRecords("lab measurement", episodes, ::labMeasurementRecordsFromEpisode)
        writeRecords("surgery", episodes, ::surgeryRecordsFromEpisode)
        
        val systemicTreatmentSchemes = writeRecordsAndReturnIndexedList(
            "systemic treatment scheme", episodes, Tables.SYSTEMICTREATMENTSCHEME, ::systemicTreatmentSchemesFromEpisode
        )
        writeRecords("systemic treatment component", systemicTreatmentSchemes, ::systemicTreatmentComponentRecordsFromScheme)
        writeRecords("PFS measure", episodes, ::pfsMeasureRecordsFromEpisode)
        
        writeDrugs()
        writeLocations()
        
        connection.setAutoCommit(true)
        context.execute("SET FOREIGN_KEY_CHECKS = 1;")
    }
    
    private fun clearAll() {
        LOGGER.info { " Clearing all patient data" }
        DefaultSchema.DEFAULT_SCHEMA.getTables().forEach { context.truncate(it).execute() }
        connection.commit()
    }

    private fun writeReferencePatients(patientRecords: List<ReferencePatient>): IndexedList<ReferencePatient> {
        LOGGER.info { " Writing patient records" }
        val (indexedRecords, rows) = patientRecords.mapIndexed { index, record ->
            val patientId = index + 1
            val dbRecord = context.newRecord(Tables.PATIENT)
            dbRecord.from(record)
            dbRecord.set(Tables.PATIENT.ID, patientId)
            Pair(patientId, record) to dbRecord
        }.unzip()
        
        insertRows(rows, "patient")
        return indexedRecords
    }

    private fun <T, U : TableRecord<*>, V> writeRecordsAndReturnIndexedList(
        name: String, indexedRecords: IndexedList<T>, table: Table<U>, recordMapper: (Int, T) -> List<Pair<V, U>>
    ): IndexedList<V> {
        LOGGER.info { " Writing $name records" }
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
        LOGGER.info { " Writing $name records" }
        val rows = indexedRecords.flatMap { (foreignKeyId, record) -> recordMapper(foreignKeyId, record) }
        insertRows(rows, name)
    }
    
    private fun insertRows(rows: List<TableRecord<*>>, name: String) {
        context.batchInsert(rows).execute()
        connection.commit()
        LOGGER.info { "  Inserted ${rows.size} $name records" }
    }

    private fun diagnosesFromPatient(patientId: Int, patient: ReferencePatient) =
        patient.tumorEntries.map { tumorEntry ->
            val dbRecord = context.newRecord(Tables.DIAGNOSIS)
            dbRecord.from(tumorEntry.diagnosis)
            dbRecord.set(Tables.DIAGNOSIS.PATIENTID, patientId)
            dbRecord.set(Tables.DIAGNOSIS.TUMORLOCATIONS, jsonList(tumorEntry.diagnosis.tumorLocations))
            tumorEntry to dbRecord
        }

    
    private fun episodesFromTumorEntry(diagnosisId: Int, tumorEntry: TumorEntry) =
        tumorEntry.episodes.map { episode ->
            val table = Tables.EPISODE
            val dbRecord = context.newRecord(table)
            dbRecord.from(episode)
            dbRecord.set(table.DIAGNOSISID, diagnosisId)
            dbRecord.set(
                table.GASTROENTEROLOGYRESECTIONS,
                jsonList(episode.gastroenterologyResections.map(GastroenterologyResection::gastroenterologyResectionType))
            )
            dbRecord.set(
                table.METASTASESSURGERIES,
                jsonList(episode.metastasesSurgeries.map(MetastasesSurgery::metastasesSurgeryType))
            )
            dbRecord.set(table.RADIOTHERAPIES, jsonList(episode.radiotherapies.map(Radiotherapy::radiotherapyType)))
            dbRecord.set(
                table.METASTASESRADIOTHERAPIES,
                jsonList(episode.metastasesRadiotherapies.map(MetastasesRadiotherapy::metastasesRadiotherapyType))
            )
            dbRecord.set(table.RESPONSE, episode.responseMeasure?.responseMeasureType?.name)
            dbRecord.set(table.INTERVALTUMORINCIDENCERESPONSEDATE, episode.responseMeasure?.intervalTumorIncidenceResponseMeasureDate)
            
            episode.systemicTreatmentPlan?.let { plan ->
                dbRecord.set(table.SYSTEMICTREATMENTPLAN, plan.treatment.name)
                dbRecord.set(table.INTERVALTUMORINCIDENCETREATMENTPLANSTART, plan.intervalTumorIncidenceTreatmentPlanStart)
                dbRecord.set(table.INTERVALTUMORINCIDENCETREATMENTPLANSTOP, plan.intervalTumorIncidenceTreatmentPlanStop)
                dbRecord.set(table.INTERVALTREATMENTPLANSTARTLATESTALIVESTATUS, plan.intervalTreatmentPlanStartLatestAliveStatus)
                dbRecord.set(table.PFS, plan.pfs)
                dbRecord.set(table.INTERVALTREATMENTPLANSTARTRESPONSEDATE, plan.intervalTreatmentPlanStartResponseDate)
            }
            episode to dbRecord
        }
    
    private fun priorTumorRecordsFromTumorEntry(diagnosisId: Int, tumorEntry: TumorEntry) =
        tumorEntry.diagnosis.priorTumors.map { priorTumor ->
            val dbRecord = context.newRecord(Tables.PRIORTUMOR)
            dbRecord.from(priorTumor)
            dbRecord.set(Tables.PRIORTUMOR.DIAGNOSISID, diagnosisId)
            dbRecord.set(Tables.PRIORTUMOR.TUMORLOCATIONS, jsonList(priorTumor.tumorLocations))
            dbRecord.set(Tables.PRIORTUMOR.SYSTEMICTREATMENTS, jsonList(priorTumor.systemicTreatments))
            dbRecord
        }

    private fun <T> extractSimpleRecord(table: Table<*>, item: T, foreignKeyColumnName: String, foreignKeyId: Int) =
        context.newRecord(table).let { dbRecord ->
            dbRecord.from(item)
            dbRecord.set(table.field(foreignKeyColumnName, Int::class.java), foreignKeyId)
            dbRecord as TableRecord<*>
        }

    private fun metastasisRecordsFromEpisode(episodeId: Int, episode: Episode) =
        episode.metastases.map { metastasis ->
            val locationGroup = metastasis.location.locationGroup.topLevelGroup().toString() 
            val dbRecord = context.newRecord(Tables.METASTASIS)
            dbRecord.from(metastasis)
            dbRecord.set(Tables.METASTASIS.EPISODEID, episodeId)
            dbRecord.set(Tables.METASTASIS.LOCATIONGROUP, locationGroup)
            dbRecord
        }

    private fun labMeasurementRecordsFromEpisode(episodeId: Int, episode: Episode) =
        episode.labMeasurements.map { extractSimpleRecord(Tables.LABMEASUREMENT, it, "episodeId", episodeId) }
    
    private fun surgeryRecordsFromEpisode(episodeId: Int, episode: Episode) =
        episode.surgeries.map { extractSimpleRecord(Tables.SURGERY, it, "episodeId", episodeId) }

    private fun systemicTreatmentSchemesFromEpisode(episodeId: Int, episode: Episode) =
        episode.systemicTreatmentPlan?.systemicTreatmentSchemes?.map { scheme ->
            val dbRecord = context.newRecord(Tables.SYSTEMICTREATMENTSCHEME)
            dbRecord.from(scheme)
            dbRecord.set(Tables.SYSTEMICTREATMENTSCHEME.EPISODEID, episodeId)
            scheme to dbRecord
        } ?: emptyList()

    private fun pfsMeasureRecordsFromEpisode(episodeId: Int, episode: Episode) =
        episode.pfsMeasures.map { extractSimpleRecord(Tables.PFSMEASURE, it, "episodeId", episodeId) }

    private fun systemicTreatmentComponentRecordsFromScheme(schemeId: Int, scheme: SystemicTreatmentScheme) =
        scheme.treatmentComponents.map { extractSimpleRecord(Tables.SYSTEMICTREATMENTCOMPONENT, it, "systemicTreatmentSchemeId", schemeId) }
    
    private fun writeDrugs() {
        LOGGER.info { " Writing drug records" }
        val rows = Drug.entries.map { drug ->
            val dbRecord = context.newRecord(Tables.DRUG)
            dbRecord.set(Tables.DRUG.NAME, drug.toString())
            dbRecord.set(Tables.DRUG.TREATMENTCATEGORY, drug.category.toString())
            dbRecord
        }
        insertRows(rows, "drug")
    }

    private fun writeLocations() {
        LOGGER.info { " Writing location records" }
        val rows = Location.entries.map { location ->
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
        private val LOGGER = KotlinLogging.logger {}

        fun fromCredentials(user: String, pass: String, url: String): DatabaseWriter {
            // Disable annoying jooq self-ad messages
            System.setProperty("org.jooq.no-logo", "true")
            System.setProperty("org.jooq.no-tips", "true")
            val conn = DriverManager.getConnection("jdbc:$url", user, pass)

            LOGGER.info { "Connecting to database '${conn.catalog}'" }
            val context = DSL.using(conn, SQLDialect.MYSQL)
            return DatabaseWriter(context, conn)
        }
    }
}
