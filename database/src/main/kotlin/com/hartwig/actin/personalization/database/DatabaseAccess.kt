package com.hartwig.actin.personalization.database

import com.hartwig.actin.personalization.datamodel.PatientRecord

import org.apache.logging.log4j.LogManager
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.MappedSchema
import org.jooq.conf.RenderMapping
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import java.sql.DriverManager

class DatabaseAccess(private val context: DSLContext) {
    
    fun writePatientRecords(patientRecords: List<PatientRecord>) {
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
        
        val dbRecords = patientRecords.mapIndexed { id, record ->
            val dbRecord = context.newRecord(Tables.PATIENTRECORD)
            dbRecord.from(record)
            dbRecord.set(Tables.PATIENTRECORD.ID, id + 1)
            id to dbRecord
        }.toMap()
        context.batchInsert(dbRecords.values).execute()
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