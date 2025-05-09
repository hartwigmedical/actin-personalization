package com.hartwig.actin.personalization.database

import com.hartwig.actin.personalization.database.tables.records.MetastasisRecord
import com.hartwig.actin.personalization.database.tables.records.MetastaticdiagnosisRecord
import com.hartwig.actin.personalization.database.tables.records.PrimarydiagnosisRecord
import com.hartwig.actin.personalization.database.tables.records.PriortumorRecord
import com.hartwig.actin.personalization.database.tables.records.SurvivalmeasurementRecord
import com.hartwig.actin.personalization.database.tables.records.TumorRecord
import com.hartwig.actin.personalization.database.tables.records.WhoassessmentRecord
import com.hartwig.actin.personalization.datamodel.ReferencePatient
import com.hartwig.actin.personalization.datamodel.TestReferencePatientFactory
import com.hartwig.actin.personalization.datamodel.Tumor
import com.hartwig.actin.personalization.datamodel.assessment.WhoAssessment
import com.hartwig.actin.personalization.datamodel.diagnosis.Metastasis
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastaticDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.PrimaryDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.PriorTumor
import com.hartwig.actin.personalization.datamodel.outcome.SurvivalMeasurement
import org.assertj.core.api.Assertions.assertThat
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.jupiter.api.Test
import org.testcontainers.containers.MySQLContainer
import java.sql.DriverManager

class MySQLTestContainer : MySQLContainer<MySQLTestContainer>("mysql:8.4.4")

class DatabaseWriterTest {

    private val exhaustiveTumorDiagnosisYear = 1961

    private val mysqlContainer = MySQLTestContainer().apply {
        withDatabaseName("testdb")
        withUsername("test")
        withInitScript("generate_database.sql")
        withTmpFs(mapOf("/var/lib/mysql" to "rw"))
        start()
    }

    private val connection = DriverManager.getConnection(mysqlContainer.jdbcUrl, mysqlContainer.username, mysqlContainer.password)
    private val dslContext = DSL.using(connection, SQLDialect.MYSQL)
    private val writer = DatabaseWriter(dslContext, connection)

    @Test
    fun `Should verify that database and all tables are created properly`() {
        val tables = dslContext.meta().schemas.flatMap { it.tables }.map { it.name }.toSet()
        assertThat(tables).containsAll(DefaultSchema.DEFAULT_SCHEMA.tables.map { it.name })
    }

    @Test
    fun `Should insert patients with empty data`() {
        val records = listOf(TestReferencePatientFactory.emptyReferencePatient())

        writer.writeAllToDb(records)
        comparePatients(records)
    }

    @Test
    fun `Should insert patients with minimum data`() {
        val records = listOf(TestReferencePatientFactory.minimalReferencePatient())

        writer.writeAllToDb(records)
        comparePatients(records)
    }

    @Test
    fun `Should insert patients with exhaustive data`() {
        val records = listOf(TestReferencePatientFactory.exhaustiveReferencePatient())

        writer.writeAllToDb(records)
        comparePatients(records)
        compareTumorForPatient(records.first(), exhaustiveTumorDiagnosisYear)
    }

    @Test
    fun `Should insert data in all tables for multiple reference patients`() {
        val records = listOf(
            TestReferencePatientFactory.emptyReferencePatient(),
            TestReferencePatientFactory.minimalReferencePatient(),
            TestReferencePatientFactory.exhaustiveReferencePatient(),
            TestReferencePatientFactory.referencePatientWithMultipleTumors()
        )

        writer.writeAllToDb(records)
        comparePatients(records)
    }

    private fun comparePatients(expectedPatients: List<ReferencePatient>) {
        val patientRecords = dslContext.selectFrom(Tables.PATIENT).fetch()
        assertThat(patientRecords.size).isEqualTo(expectedPatients.size)
        expectedPatients.mapIndexed { index, patient ->
            val patientRecord = dslContext.selectFrom(Tables.PATIENT).where(Tables.PATIENT.ID.eq(index + 1)).fetchOne()
            assertThat(patientRecord).isNotNull()

            assertThat(patientRecord!!.get(Tables.PATIENT.SEX)).isEqualTo(patient.sex.name)
            val patientId = patientRecord.get(Tables.PATIENT.ID)

            val tumorRecords = dslContext.selectFrom(Tables.TUMOR).where(Tables.TUMOR.PATIENTID.eq(patientId)).fetch()
            assertThat(tumorRecords.size).isEqualTo(patient.tumors.size)
        }
    }

    private fun compareTumorForPatient(patient: ReferencePatient, expectedDiagnosisYear: Int) {
        patient.tumors.first() { it.diagnosisYear == expectedDiagnosisYear }.let { expectedTumor ->
            val expectedTumorRecord = dslContext.selectFrom(Tables.TUMOR)
                .where(Tables.TUMOR.DIAGNOSISYEAR.eq(expectedDiagnosisYear)).fetchOne()
            assertThat(expectedTumorRecord).isNotNull()
            compareTumor(expectedTumorRecord!!, expectedTumor)
        }
    }

    private fun compareTumor(record: TumorRecord, expectedTumor: Tumor) {
        assertThat(record.get(Tables.TUMOR.DIAGNOSISYEAR)).isEqualTo(expectedTumor.diagnosisYear)
        assertThat(record.get(Tables.TUMOR.AGEATDIAGNOSIS)).isEqualTo(expectedTumor.ageAtDiagnosis)

        val tumorId = record.get(Tables.TUMOR.ID)
        compareSurvivalMeasurements(tumorId, expectedTumor)
        comparePriorTumors(tumorId, expectedTumor)
        comparePrimaryDiagnosis(tumorId, expectedTumor)
        compareMetastaticDiagnosis(tumorId, expectedTumor)
        compareWhoAssessments(tumorId, expectedTumor)
        compareAsaAssessments(tumorId, expectedTumor)
        compareMolecularResults(tumorId, expectedTumor)
        compareLabMeasurements(tumorId, expectedTumor)
        compareTreatmentEpisodes(tumorId, expectedTumor)
    }
    
    private fun compareSurvivalMeasurements(tumorId: Int, expectedTumor: Tumor) {
        val survivalMeasurementRecords =
            dslContext.selectFrom(Tables.SURVIVALMEASUREMENT).where(Tables.SURVIVALMEASUREMENT.TUMORID.eq(tumorId)).fetchOne()

        assertThat(survivalMeasurementRecords).isNotNull()
        compare(survivalMeasurementRecords!!, expectedTumor.latestSurvivalMeasurement)
    }

    private fun compare(record: SurvivalmeasurementRecord, survivalMeasure: SurvivalMeasurement) {
        assertThat(record.get(Tables.SURVIVALMEASUREMENT.DAYSSINCEDIAGNOSIS)).isEqualTo(survivalMeasure.daysSinceDiagnosis)
        assertThat(record.get(Tables.SURVIVALMEASUREMENT.ISALIVE)).isEqualTo(survivalMeasure.isAlive)
    }

    private fun comparePriorTumors(tumorId: Int, expectedTumor: Tumor) {
        val priorTumorsRecords = dslContext.selectFrom(Tables.PRIORTUMOR).where(Tables.PRIORTUMOR.TUMORID.eq(tumorId)).fetch()
        assertThat(priorTumorsRecords.size).isEqualTo(expectedTumor.priorTumors.size)
        expectedTumor.priorTumors.mapIndexed { index, priorTumor ->
            val priorTumorRecord = dslContext.selectFrom(Tables.PRIORTUMOR)
                .where(Tables.PRIORTUMOR.ID.eq(index + 1).and(Tables.PRIORTUMOR.TUMORID.eq(tumorId))).fetchOne()
            assertThat(priorTumorRecord).isNotNull()
            compare(priorTumorRecord!!, priorTumor)
        }
    }

    private fun compare(record: PriortumorRecord, expected: PriorTumor) {
        val table = Tables.PRIORTUMOR
        assertThat(record.get(table.DAYSBEFOREDIAGNOSIS) ?: null).isEqualTo(expected.daysBeforeDiagnosis)
        assertThat(record.get(table.PRIMARYTUMORTYPE)).isEqualTo(expected.primaryTumorType.name)
        assertThat(record.get(table.PRIMARYTUMORLOCATION)).isEqualTo(expected.primaryTumorLocation.name)
        assertThat(record.get(table.PRIMARYTUMORLOCATIONCATEGORY)).isEqualTo(expected.primaryTumorLocationCategory.name)
        assertThat(record.get(table.PRIMARYTUMORSTAGE ?: null)).isEqualTo(expected.primaryTumorStage?.name)
        assertThat(record.get(table.SYSTEMICDRUGSRECEIVED ?: null)).isEqualTo(DatabaseWriter.concat(expected.systemicDrugsReceived))
    }

    private fun comparePrimaryDiagnosis(tumorId: Int, expectedTumor: Tumor) {
        val primaryDiagnosisRecord =
            dslContext.selectFrom(Tables.PRIMARYDIAGNOSIS).where(Tables.PRIMARYDIAGNOSIS.TUMORID.eq(tumorId)).fetchOne()
        assertThat(primaryDiagnosisRecord).isNotNull()
        compare(primaryDiagnosisRecord!!, expectedTumor.primaryDiagnosis)
    }

    private fun compare(record: PrimarydiagnosisRecord, expected: PrimaryDiagnosis) {
        val table = Tables.PRIMARYDIAGNOSIS
        assertThat(record.get(table.BASISOFDIAGNOSIS) ?: null).isEqualTo(expected.basisOfDiagnosis.name)
        assertThat(record.get(table.HASDOUBLEPRIMARYTUMOR) ?: null).isEqualTo(expected.hasDoublePrimaryTumor)
        assertThat(record.get(table.PRIMARYTUMORTYPE) ?: null).isEqualTo(expected.primaryTumorType.name)
        assertThat(record.get(table.PRIMARYTUMORLOCATION) ?: null).isEqualTo(expected.primaryTumorLocation.name)
        assertThat(record.get(table.SIDEDNESS) ?: null).isEqualTo(expected.sidedness?.name)

        assertThat(record.get(table.ANORECTALVERGEDISTANCECATEGORY) ?: null).isEqualTo(expected.anorectalVergeDistanceCategory?.name)
        assertThat(record.get(table.MESORECTALFASCIAISCLEAR) ?: null).isEqualTo(expected.mesorectalFasciaIsClear)
        assertThat(record.get(table.DISTANCETOMESORECTALFASCIAMM) ?: null).isEqualTo(expected.distanceToMesorectalFasciaMm)

        assertThat(record.get(table.DIFFERENTIATIONGRADE) ?: null).isEqualTo(expected.differentiationGrade?.name)
        assertThat(record.get(table.CLINICALTNMT) ?: null).isEqualTo(expected.clinicalTnmClassification.tnmT?.name)
        assertThat(record.get(table.CLINICALTNMN) ?: null).isEqualTo(expected.clinicalTnmClassification.tnmN?.name)
        assertThat(record.get(table.CLINICALTNMM) ?: null).isEqualTo(expected.clinicalTnmClassification.tnmM?.name)
        assertThat(record.get(table.PATHOLOGICALTNMT) ?: null).isEqualTo(expected.pathologicalTnmClassification.tnmT?.name)
        assertThat(record.get(table.PATHOLOGICALTNMN) ?: null).isEqualTo(expected.pathologicalTnmClassification.tnmN?.name)
        assertThat(record.get(table.PATHOLOGICALTNMM) ?: null).isEqualTo(expected.pathologicalTnmClassification.tnmM?.name)
        assertThat(record.get(table.CLINICALTUMORSTAGE) ?: null).isEqualTo(expected.clinicalTumorStage.name)
        assertThat(record.get(table.PATHOLOGICALTUMORSTAGE) ?: null).isEqualTo(expected.pathologicalTumorStage.name)
        assertThat(record.get(table.INVESTIGATEDLYMPHNODESCOUNT) ?: null).isEqualTo(expected.investigatedLymphNodesCount)
        assertThat(record.get(table.POSITIVELYMPHNODESCOUNT) ?: null).isEqualTo(expected.positiveLymphNodesCount)

        assertThat(record.get(table.PRESENTEDWITHILEUS) ?: null).isEqualTo(expected.presentedWithIleus)
        assertThat(record.get(table.PRESENTEDWITHPERFORATION) ?: null).isEqualTo(expected.presentedWithPerforation)

        assertThat(record.get(table.VENOUSINVASIONDESCRIPTION) ?: null).isEqualTo(expected.venousInvasionDescription?.name)
        assertThat(record.get(table.LYMPHATICINVASIONCATEGORY) ?: null).isEqualTo(expected.lymphaticInvasionCategory?.name)
        assertThat(record.get(table.EXTRAMURALINVASIONCATEGORY) ?: null).isEqualTo(expected.extraMuralInvasionCategory?.name)
        assertThat(record.get(table.TUMORREGRESSION) ?: null).isEqualTo(expected.tumorRegression?.name)
    }

    private fun compareMetastaticDiagnosis(tumorId: Int, expectedTumor: Tumor) {
        val metastaticDiagnosisRecord =
            dslContext.selectFrom(Tables.METASTATICDIAGNOSIS).where(Tables.METASTATICDIAGNOSIS.TUMORID.eq(tumorId)).fetchOne()
        assertThat(metastaticDiagnosisRecord).isNotNull()
        compare(metastaticDiagnosisRecord!!, expectedTumor.metastaticDiagnosis)
    }

    private fun compare(record: MetastaticdiagnosisRecord, expected: MetastaticDiagnosis) {
        val table = Tables.METASTATICDIAGNOSIS
        assertThat(record.get(table.ISMETACHRONOUS)).isEqualTo(expected.isMetachronous)
        assertThat(record.get(table.NUMBEROFLIVERMETASTASES) ?: null).isEqualTo(expected.numberOfLiverMetastases?.name)
        assertThat(record.get(table.MAXIMUMSIZEOFLIVERMETASTASISMM) ?: null).isEqualTo(expected.maximumSizeOfLiverMetastasisMm)
        assertThat(record.get(table.INVESTIGATEDLYMPHNODESCOUNT) ?: null).isEqualTo(expected.investigatedLymphNodesCount)
        assertThat(record.get(table.POSITIVELYMPHNODESCOUNT) ?: null).isEqualTo(expected.positiveLymphNodesCount)

        expected.metastases.mapIndexed { index, metastasis ->
            val metastasisRecord = dslContext.selectFrom(Tables.METASTASIS)
                .where(Tables.METASTASIS.ID.eq(index + 1).and(Tables.METASTASIS.METASTATICDIAGNOSISID.eq(record.get(table.ID)))).fetchOne()
            assertThat(metastasisRecord).isNotNull()
            compare(metastasisRecord!!, metastasis)
        }
    }

    private fun compare(record: MetastasisRecord, expected: Metastasis) {
        val table = Tables.METASTASIS
        assertThat(record.get(table.DAYSSINCEDIAGNOSIS) ?: null).isEqualTo(expected.daysSinceDiagnosis)
        assertThat(record.get(table.LOCATION)).isEqualTo(expected.location.name)
        assertThat(record.get(table.ISLINKEDTOPROGRESSION) ?: null).isEqualTo(expected.isLinkedToProgression)
    }
    
    private fun compareWhoAssessments(tumorId: Int, expectedTumor: Tumor) {
        val whoAssessments = dslContext.selectFrom(Tables.WHOASSESSMENT).where(Tables.WHOASSESSMENT.TUMORID.eq(tumorId)).fetch()
        assertThat(whoAssessments.size).isEqualTo(expectedTumor.whoAssessments.size)
        expectedTumor.whoAssessments.mapIndexed { index, whoAssessment ->
            val whoAssessmentRecord = dslContext.selectFrom(Tables.WHOASSESSMENT)
                .where(Tables.WHOASSESSMENT.ID.eq(index + 1).and(Tables.WHOASSESSMENT.TUMORID.eq(tumorId))).fetchOne()
            assertThat(whoAssessmentRecord).isNotNull()
            compare(whoAssessmentRecord!!, whoAssessment)
        }
    }

    private fun compare(record: WhoassessmentRecord, expected: WhoAssessment) {
        val table = Tables.WHOASSESSMENT
        assertThat(record.get(table.DAYSSINCEDIAGNOSIS)).isEqualTo(expected.daysSinceDiagnosis)
        assertThat(record.get(table.WHOSTATUS)).isEqualTo(expected.whoStatus)
    }

    private fun compareAsaAssessments(tumorId: Int, expectedTumor: Tumor) {
        // TODO (KD): Implement
    }

    private fun compareMolecularResults(tumorId: Int, expectedTumor: Tumor) {
        // TODO (KD): Implement
    }

    private fun compareLabMeasurements(tumorId: Int, expectedTumor: Tumor) {
        // TODO (KD): Implement
    }

    private fun compareTreatmentEpisodes(tumorId: Int, expectedTumor: Tumor) {
        // TODO (KD): Implement
    }
}