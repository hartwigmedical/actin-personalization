package com.hartwig.actin.personalization.database

import com.hartwig.actin.personalization.datamodel.Diagnosis
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.GastroenterologyResection
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
import org.jooq.impl.DSL
import java.sql.DriverManager

private typealias IndexedList<T> = List<Pair<Int, T>>

class DatabaseAccess(private val context: DSLContext) {
    
    fun writeAllToDb(patientRecords: List<PatientRecord>) {
        clearAll()
        
        val indexedRecords = writePatientRecords(patientRecords)
        val tumorEntries = writeDiagnoses(indexedRecords)
        val episodes = writeEpisodes(tumorEntries)
        writePriorTumors(tumorEntries)
        writeLabMeasurements(episodes)
        writeSurgeries(episodes)
        val systemicTreatmentSchemes = writeSystemicTreatmentSchemes(episodes)
        writeSystemicTreatmentComponents(systemicTreatmentSchemes)
        writePfsMeasures(systemicTreatmentSchemes)
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

    private fun writeDiagnoses(patientRecords: IndexedList<PatientRecord>): IndexedList<TumorEntry> {
        LOGGER.info(" Writing diagnosis records")
        val (tumorEntries, rows) = patientRecords.flatMap { (patientId, patient) ->
            patient.tumorEntries.map { tumorEntry ->
                val dbRecord = context.newRecord(Tables.DIAGNOSIS)
                dbRecord.from(tumorEntry.diagnosis)
                dbRecord.set(Tables.DIAGNOSIS.PATIENTRECORDID, patientId)
                dbRecord.set(Tables.DIAGNOSIS.TUMORLOCATIONS, jsonList(tumorEntry.diagnosis.tumorLocations))
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

    
    private fun writeEpisodes(tumorEntries: IndexedList<TumorEntry>): IndexedList<Episode> {
        LOGGER.info(" Writing episode records")
        val (episodeRecords, rows) = tumorEntries.flatMap { (diagnosisId, tumorEntry) ->
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
    
    private fun writePriorTumors(tumorEntries: IndexedList<TumorEntry>) {
        LOGGER.info(" Writing prior tumor records")
        val rows = tumorEntries.flatMap { (diagnosisId, tumorEntry) ->
            tumorEntry.diagnosis.priorTumors.map { priorTumor ->
                val dbRecord = context.newRecord(Tables.PRIORTUMOR)
                dbRecord.from(priorTumor)
                dbRecord.set(Tables.PRIORTUMOR.DIAGNOSISID, diagnosisId)
                dbRecord.set(Tables.PRIORTUMOR.TUMORLOCATIONS, jsonList(priorTumor.tumorLocations))
                dbRecord.set(Tables.PRIORTUMOR.SYSTEMICTREATMENTS, jsonList(priorTumor.systemicTreatments))
                dbRecord
            }
        }
        context.batchInsert(rows).execute()
    }
    
    private fun writeLabMeasurements(episodes: IndexedList<Episode>) {
        LOGGER.info(" Writing lab measurement records")
        val rows = episodes.flatMap { (episodeId, episode) ->
            episode.labMeasurements.map { labMeasurement ->
                val dbRecord = context.newRecord(Tables.LABMEASUREMENT)
                dbRecord.from(labMeasurement)
                dbRecord.set(Tables.LABMEASUREMENT.EPISODEID, episodeId)
                dbRecord
            }
        }
        context.batchInsert(rows).execute()
    }
    
    private fun writeSurgeries(episodes: IndexedList<Episode>) {
        LOGGER.info(" Writing surgery records")
        val rows = episodes.flatMap { (episodeId, episode) ->
            episode.surgeries.map { surgery ->
                val dbRecord = context.newRecord(Tables.SURGERY)
                dbRecord.from(surgery)
                dbRecord.set(Tables.SURGERY.EPISODEID, episodeId)
                dbRecord
            }
        }
        context.batchInsert(rows).execute()
    }

    private fun writeSystemicTreatmentSchemes(episodes: IndexedList<Episode>): IndexedList<SystemicTreatmentScheme> {
        LOGGER.info(" Writing systemic treatment scheme records")
        val (schemeEntries, rows) = episodes.flatMap { (episodeId, episode) ->
            episode.systemicTreatmentSchemes.map { scheme ->
                val dbRecord = context.newRecord(Tables.SYSTEMICTREATMENTSCHEME)
                dbRecord.from(scheme)
                dbRecord.set(Tables.SYSTEMICTREATMENTSCHEME.EPISODEID, episodeId)
                scheme to dbRecord
            }
        }
            .mapIndexed { index, (scheme, dbRecord) ->
                val schemeId = index + 1
                dbRecord.set(Tables.SYSTEMICTREATMENTSCHEME.ID, schemeId)
                Pair(schemeId, scheme) to dbRecord
            }
            .unzip()

        context.batchInsert(rows).execute()
        return schemeEntries
    }
    
    private fun writeSystemicTreatmentComponents(schemeEntries: IndexedList<SystemicTreatmentScheme>) {
        LOGGER.info(" Writing systemic treatment component records")
        val rows = schemeEntries.flatMap { (schemeId, scheme) ->
            scheme.treatmentComponents.map { component ->
                val dbRecord = context.newRecord(Tables.SYSTEMICTREATMENTCOMPONENT)
                dbRecord.from(component)
                dbRecord.set(Tables.SYSTEMICTREATMENTCOMPONENT.SYSTEMICTREATMENTSCHEMEID, schemeId)
                dbRecord
            }
        }
        context.batchInsert(rows).execute()
    }
    
    private fun writePfsMeasures(schemeEntries: IndexedList<SystemicTreatmentScheme>) {
        LOGGER.info(" Writing PFS measure records")
        val rows = schemeEntries.flatMap { (schemeId, scheme) ->
            scheme.treatmentRawPfs.map { pfsMeasure ->
                val dbRecord = context.newRecord(Tables.PFSMEASURE)
                dbRecord.from(pfsMeasure)
                dbRecord.set(Tables.PFSMEASURE.SYSTEMICTREATMENTSCHEMEID, schemeId)
                dbRecord
            }
        }
        context.batchInsert(rows).execute()
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
            val jdbcUrl = "jdbc:$url"
            val conn = DriverManager.getConnection(jdbcUrl, user, pass)
            val catalog = conn.catalog
            LOGGER.info("Connecting to database '{}'", catalog)
            val context = DSL.using(conn, SQLDialect.MYSQL)
            return DatabaseAccess(context)
        }
    }
}
