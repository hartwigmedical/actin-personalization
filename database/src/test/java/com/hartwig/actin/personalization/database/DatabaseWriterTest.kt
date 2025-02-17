package com.hartwig.actin.personalization.database


import com.hartwig.actin.personalization.database.tables.records.MetastaticdiagnosisRecord
import com.hartwig.actin.personalization.database.tables.records.PatientRecord
import com.hartwig.actin.personalization.database.tables.records.PrimarydiagnosisRecord
import com.hartwig.actin.personalization.database.tables.records.PriortumorRecord
import com.hartwig.actin.personalization.database.tables.records.SurvivalmeasureRecord
import com.hartwig.actin.personalization.database.tables.records.TumorRecord
import com.hartwig.actin.personalization.datamodel.ReferencePatient
import com.hartwig.actin.personalization.datamodel.Tumor
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastaticDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.PrimaryDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.PriorTumor
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmClassification
import com.hartwig.actin.personalization.datamodel.outcome.SurvivalMeasure
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.jooq.DSLContext
import org.jooq.JSON
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
    fun `Should insert patients with minimum data`() {
        writer.writeAllToDb(PATIENT_RECORDS_MINIMUM)
        compare(PATIENT_RECORDS_MINIMUM)
    }

    @Test
    fun `Should insert data in all tables`() {
        writer.writeAllToDb(PATIENT_RECORDS_COMPLETE)
        compare(PATIENT_RECORDS_COMPLETE)
    }

    private fun compare(expectedPatients: List<ReferencePatient>) {
        val patientRecords = dslContext.selectFrom(Tables.PATIENT).fetch()
        assertThat(patientRecords.size).isEqualTo(expectedPatients.size)
        expectedPatients.mapIndexed { index, patient ->
            val record = dslContext.selectFrom(Tables.PATIENT).where(Tables.PATIENT.ID.eq(index + 1)).fetchOne()
            assertThat(record).isNotNull()
            compare(record!!, patient)
        }
    }

    private fun compare(patientRecord: PatientRecord, expectedPatient: ReferencePatient) {

        assertThat(patientRecord.get(Tables.PATIENT.SEX)).isEqualTo(expectedPatient.sex.name)
        val tumorRecords = dslContext.selectFrom(Tables.TUMOR).fetch()

        assertThat(tumorRecords.size).isEqualTo(expectedPatient.tumors.size)

        val patientId = patientRecord.get(Tables.PATIENT.ID)

        expectedPatient.tumors.mapIndexed { index, tumor ->
            val record = dslContext.selectFrom(Tables.TUMOR).where(
                Tables.TUMOR.ID.eq(index + 1).and(Tables.TUMOR.PATIENTID.eq(patientId))
            ).fetchOne()
            assertThat(record).isNotNull()
            compare(record!!, tumor)
        }
    }

    private fun compare(record: TumorRecord, expectedTumor: Tumor) {
        assertThat(record.get(Tables.TUMOR.DIAGNOSISYEAR)).isEqualTo(expectedTumor.diagnosisYear)
        assertThat(record.get(Tables.TUMOR.AGEATDIAGNOSIS)).isEqualTo(expectedTumor.ageAtDiagnosis)
        assertThat(record.get(Tables.TUMOR.HASRECEIVEDTUMORDIRECTEDTREATMENT)).isEqualTo(expectedTumor.hasReceivedTumorDirectedTreatment)
        val tumorId = record.get(Tables.TUMOR.ID)

        val survivalMeasureRecords =
            dslContext.selectFrom(Tables.SURVIVALMEASURE).where(Tables.SURVIVALMEASURE.TUMORID.eq(tumorId)).fetchOne()

        assertThat(survivalMeasureRecords).isNotNull()
        compare(survivalMeasureRecords!!, expectedTumor.latestSurvivalStatus)

        val priorTumorsRecords = dslContext.selectFrom(Tables.PRIORTUMOR).where(Tables.PRIORTUMOR.TUMORID.eq(tumorId)).fetch()
        assertThat(priorTumorsRecords.size).isEqualTo(expectedTumor.priorTumors.size)
        expectedTumor.priorTumors.mapIndexed { index, priorTumor ->
            val record = dslContext.selectFrom(Tables.PRIORTUMOR)
                .where(Tables.PRIORTUMOR.ID.eq(index + 1).and(Tables.PRIORTUMOR.TUMORID.eq(tumorId))).fetchOne()
            assertThat(record).isNotNull()
            compare(record!!, priorTumor)
        }

        val primaryDiagnosisRecord =
            dslContext.selectFrom(Tables.PRIMARYDIAGNOSIS).where(Tables.PRIMARYDIAGNOSIS.TUMORID.eq(tumorId)).fetchOne()
        assertThat(primaryDiagnosisRecord).isNotNull()
        compare(primaryDiagnosisRecord!!, expectedTumor.primaryDiagnosis)

        val metastaticDiagnosisRecord =
            dslContext.selectFrom(Tables.METASTATICDIAGNOSIS).where(Tables.METASTATICDIAGNOSIS.TUMORID.eq(tumorId)).fetchOne()
        assertThat(metastaticDiagnosisRecord).isNotNull()
        compare(metastaticDiagnosisRecord!!, expectedTumor.metastaticDiagnosis)

        // TODO validate whoAssessments
        // TODO validate asaAssessments
        // TODO validate comorbidityAssessments
        // TODO validate molecularResults
        // TODO validate labMeasurements
        // TODO validate gastroenterologyResections
        // TODO validate primarySurgeries
        // TODO validate metastaticSurgeries

        val hipecTreatmentRecords = dslContext.selectFrom(Tables.HIPECTREATMENT).where(Tables.HIPECTREATMENT.TUMORID.eq(tumorId)).fetchOne()
        assertThat(hipecTreatmentRecords).isNotNull()
        // TODO compare hipecTreatment

        // TODO validate primaryRadiotherapies
        // TODO validate metastaticRadiotherapies
        // TODO validate systemicTreatments
        // TODO validate responseMeasures
        // TODO validate progressionMeasures


    }

    private fun compare(record: PriortumorRecord, expected: PriorTumor) {
        assertThat(record.get(Tables.PRIORTUMOR.DAYSBEFOREDIAGNOSIS) ?: null).isEqualTo(expected.daysBeforeDiagnosis)
        assertThat(record.get(Tables.PRIORTUMOR.PRIMARYTUMORTYPE)).isEqualTo(expected.primaryTumorType.name)
        assertThat(record.get(Tables.PRIORTUMOR.PRIMARYTUMORLOCATION)).isEqualTo(expected.primaryTumorLocation.name)
        assertThat(record.get(Tables.PRIORTUMOR.PRIMARYTUMORLOCATIONCATEGORY)).isEqualTo(expected.primaryTumorLocationCategory.name)
        assertThat(record.get(Tables.PRIORTUMOR.PRIMARYTUMORSTAGE ?: null)).isEqualTo(expected.primaryTumorStage?.name)
        // TODO JSON assertThat(record.get(Tables.PRIORTUMOR.SYSTEMICDRUGSRECEIVED))
    }

    private fun compare(record: PrimarydiagnosisRecord, expected: PrimaryDiagnosis) {
        val table = Tables.PRIMARYDIAGNOSIS
        assertThat(record.get(table.BASISOFDIAGNOSIS) ?: null).isEqualTo(expected.basisOfDiagnosis.name)
        assertThat(record.get(table.HASDOUBLEPRIMARYTUMOR) ?: null).isEqualTo(expected.hasDoublePrimaryTumor)
        assertThat(record.get(table.PRIMARYTUMORTYPE) ?: null).isEqualTo(expected.primaryTumorType.name)
        assertThat(record.get(table.PRIMARYTUMORLOCATION) ?: null).isEqualTo(expected.primaryTumorLocation.name)
        assertThat(record.get(table.DIFFERENTIATIONGRADE) ?: null).isEqualTo(expected.differentiationGrade?.name)

        compare(record.get(table.CLINICALTNMCLASSIFICATION), expected.clinicalTnmClassification)
        compare(record.get(table.PATHOLOGICALTNMCLASSIFICATION), expected.pathologicalTnmClassification)
        assertThat(record.get(table.CLINICALTUMORSTAGE) ?: null).isEqualTo(expected.clinicalTumorStage?.name)
        assertThat(record.get(table.PATHOLOGICALTUMORSTAGE) ?: null).isEqualTo(expected.pathologicalTumorStage?.name)
        assertThat(record.get(table.INVESTIGATEDLYMPHNODESCOUNT) ?: null).isEqualTo(expected.investigatedLymphNodesCount)
        assertThat(record.get(table.POSITIVELYMPHNODESCOUNT) ?: null).isEqualTo(expected.positiveLymphNodesCount)

        assertThat(record.get(table.SIDEDNESS) ?: null).isEqualTo(expected.sidedness?.name)
        assertThat(record.get(table.PRESENTEDWITHILEUS) ?: null).isEqualTo(expected.presentedWithIleus)
        assertThat(record.get(table.PRESENTEDWITHPERFORATION) ?: null).isEqualTo(expected.presentedWithPerforation)

        assertThat(record.get(table.ANORECTALVERGEDISTANCECATEGORY) ?: null).isEqualTo(expected.anorectalVergeDistanceCategory?.name)
        assertThat(record.get(table.MESORECTALFASCIAISCLEAR) ?: null).isEqualTo(expected.mesorectalFasciaIsClear)
        assertThat(record.get(table.DISTANCETOMESORECTALFASCIAMM) ?: null).isEqualTo(expected.distanceToMesorectalFasciaMm)

        assertThat(record.get(table.VENOUSINVASIONDESCRIPTION) ?: null).isEqualTo(expected.venousInvasionDescription?.name)
        assertThat(record.get(table.LYMPHATICINVASIONCATEGORY) ?: null).isEqualTo(expected.lymphaticInvasionCategory?.name)
        assertThat(record.get(table.EXTRAMURALINVASIONCATEGORY) ?: null).isEqualTo(expected.extraMuralInvasionCategory?.name)
        assertThat(record.get(table.TUMORREGRESSION) ?: null).isEqualTo(expected.tumorRegression?.name)
    }

    private fun compare(record: MetastaticdiagnosisRecord, expected: MetastaticDiagnosis){
        val table = Tables.METASTATICDIAGNOSIS
        assertThat(record.get(table.DISTANTMETASTASESDETECTIONSTATUS) ?: null).isEqualTo(expected.distantMetastasesDetectionStatus.name)
        assertThat(record.get(table.NUMBEROFLIVERMETASTASES) ?: null).isEqualTo(expected.numberOfLiverMetastases?.name)
        assertThat(record.get(table.MAXIMUMSIZEOFLIVERMETASTASISMM) ?: null).isEqualTo(expected.maximumSizeOfLiverMetastasisMm)
        assertThat(record.get(table.INVESTIGATEDLYMPHNODESCOUNT) ?: null).isEqualTo(expected.investigatedLymphNodesCount)
        assertThat(record.get(table.POSITIVELYMPHNODESCOUNT) ?: null).isEqualTo(expected.positiveLymphNodesCount)
        // TODO validate metastases
    }

    private fun compare(json: JSON?, expected: TnmClassification?) {
        if (expected != null) {
            assertThat(Json.decodeFromString<TnmClassification>(json!!.data())).isEqualTo(expected)
        } else {
            assertThat(json).isNull()
        }
    }

    private fun compare(record: SurvivalmeasureRecord, survivalMeasure: SurvivalMeasure) {
        assertThat(record.get(Tables.SURVIVALMEASURE.DAYSSINCEDIAGNOSIS)).isEqualTo(survivalMeasure.daysSinceDiagnosis)
        assertThat(record.get(Tables.SURVIVALMEASURE.ISALIVE)).isEqualTo(survivalMeasure.isAlive)
    }

}