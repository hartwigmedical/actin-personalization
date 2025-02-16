package com.hartwig.actin.personalization.database

import org.assertj.core.api.Assertions.assertThat
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.jupiter.api.Test
import org.testcontainers.containers.MySQLContainer
import java.sql.Connection
import java.sql.DriverManager

class MySQLTestContainer : MySQLContainer<MySQLTestContainer>("mysql:8.0")

class DatabaseWriterTest {

    private val mysqlContainer = MySQLTestContainer().apply {
        withDatabaseName("testdb")
        withUsername("test")
        withPassword("test")
        withInitScript("generate_database.sql")
        start()
    }

    private val connection: Connection = DriverManager.getConnection(
        mysqlContainer.jdbcUrl, mysqlContainer.username, mysqlContainer.password
    )

    private val dslContext: DSLContext = DSL.using(connection, SQLDialect.MYSQL)

    private val writer = DatabaseWriter(dslContext, connection)

    @Test
    fun `Should verify that database and all tables are created properly`() {
        val tables = dslContext.meta().schemas.flatMap { it.tables }.map { it.name }.toSet()
        assertThat(tables).containsAll(DefaultSchema.DEFAULT_SCHEMA.tables.map { it.name })
    }

    @Test
    fun `Should insert patients without tumors`() {
        writer.writeAllToDb(PATIENT_RECORDS_NO_TUMOR)
        val existingRecords = dslContext.selectFrom(Tables.PATIENT).fetch()
        val sexes = existingRecords.map { it.get(Tables.PATIENT.SEX) }
        assertThat(sexes).containsAll(listOf("FEMALE", "MALE"))
    }

    @Test
    fun `Should insert data in all tables`() {
        writer.writeAllToDb(PATIENT_RECORDS_COMPLETE)
        val existingRecords = dslContext.selectFrom(Tables.PATIENT).fetch()
        val sexes = existingRecords.map { it.get(Tables.PATIENT.SEX) }
        assertThat(sexes).containsAll(listOf("MALE"))
        // TODO add validations
    }

}