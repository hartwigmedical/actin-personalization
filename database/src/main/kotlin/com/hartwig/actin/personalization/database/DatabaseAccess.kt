package com.hartwig.actin.personalization.database

import org.apache.logging.log4j.LogManager
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.MappedSchema
import org.jooq.conf.RenderMapping
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import java.sql.DriverManager

class DatabaseAccess(private val context: DSLContext) {

    companion object {
        private val LOGGER = LogManager.getLogger(DatabaseAccess::class.java)
        private const val DEV_CATALOG = "actin_test"

        fun fromCredentials(user: String, pass: String, url: String): DatabaseAccess {
            // Disable annoying jooq self-ad messages
            System.setProperty("org.jooq.no-logo", "true")
            System.setProperty("org.jooq.no-tips", "true")
            val jdbcUrl = "jdbc:$url"
            val conn = DriverManager.getConnection(jdbcUrl, user, pass)
            val catalog = conn.catalog
            LOGGER.info("Connecting to database '{}'", catalog)
            val context = DSL.using(conn, SQLDialect.MYSQL, settings(catalog))
            return DatabaseAccess(context)
        }

        private fun settings(catalog: String): Settings? {
            return if (catalog != DEV_CATALOG) {
                Settings().withRenderMapping(RenderMapping().withSchemata(MappedSchema().withInput(DEV_CATALOG).withOutput(catalog)))
            } else null
        }
    }
}