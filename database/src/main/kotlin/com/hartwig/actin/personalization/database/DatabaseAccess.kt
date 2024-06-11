package com.hartwig.actin.personalization.database

import com.hartwig.actin.personalization.datamodel.Diagnosis
import com.hartwig.actin.personalization.datamodel.PatientRecord

import org.apache.logging.log4j.LogManager
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.Table
import java.sql.DriverManager

private typealias IndexedList<T> = List<Pair<Int, T>>

class DatabaseAccess(private val context: DSLContext) {
    
    fun writeAllToDb(patientRecords: List<PatientRecord>) {
        clearAll()
        val indexedRecords = writePatientRecords(patientRecords)
        val diagnoses = writeDiagnosisRecords(indexedRecords)
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

    private fun writeDiagnosisRecords(patientRecords: IndexedList<PatientRecord>): IndexedList<Diagnosis> {
        LOGGER.info(" Writing diagnosis records")
        val (diagnosisRecords, rows) = patientRecords.flatMap { (patientId, patient) ->
            patient.tumorEntries.map { (diagnosis, _) ->
                val dbRecord = context.newRecord(Tables.DIAGNOSIS)
                dbRecord.from(diagnosis)
                dbRecord.set(Tables.DIAGNOSIS.PATIENTRECORDID, patientId)
                diagnosis to dbRecord
            }
        }
            .mapIndexed { index, (diagnosis, dbRecord) ->
                val diagnosisId = index + 1
                dbRecord.set(Tables.DIAGNOSIS.ID, diagnosisId)
                Pair(diagnosisId, diagnosis) to dbRecord
            }
            .unzip()
        
        context.batchInsert(rows).execute()
        return diagnosisRecords
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
