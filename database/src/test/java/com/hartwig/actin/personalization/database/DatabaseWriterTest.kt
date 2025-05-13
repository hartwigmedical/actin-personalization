package com.hartwig.actin.personalization.database

import com.hartwig.actin.personalization.database.tables.records.MetastasisRecord
import com.hartwig.actin.personalization.database.tables.records.MetastaticdiagnosisRecord
import com.hartwig.actin.personalization.database.tables.records.PrimarydiagnosisRecord
import com.hartwig.actin.personalization.database.tables.records.PriortumorRecord
import com.hartwig.actin.personalization.database.tables.records.SurvivalmeasurementRecord
import com.hartwig.actin.personalization.database.tables.records.WhoassessmentRecord
import com.hartwig.actin.personalization.datamodel.ReferenceEntry
import com.hartwig.actin.personalization.datamodel.TestReferenceEntryFactory
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
    fun `Should insert empty reference entry`() {
        val entries = listOf(TestReferenceEntryFactory.emptyReferenceEntry())

        writer.writeAllToDb(entries)
        compareEntries(entries)
    }

    @Test
    fun `Should insert minimal reference entry`() {
        val records = listOf(TestReferenceEntryFactory.minimalReferenceEntry())

        writer.writeAllToDb(records)
        compareEntries(records)
    }

    @Test
    fun `Should insert exhaustive reference entry`() {
        val entries = listOf(TestReferenceEntryFactory.exhaustiveReferenceEntry())

        writer.writeAllToDb(entries)
        compareEntries(entries)
    }
    
    private fun compareEntries(expectedEntries: List<ReferenceEntry>) {
        val referenceEntries = dslContext.selectFrom(Tables.ENTRY).fetch()
        assertThat(referenceEntries.size).isEqualTo(expectedEntries.size)
        expectedEntries.mapIndexed { index, expected ->
            val entryId = index + 1
            val record = dslContext.selectFrom(Tables.ENTRY).where(Tables.ENTRY.ID.eq(entryId)).fetchOne()

            assertThat(record!!.get(Tables.ENTRY.SOURCE)).isEqualTo(expected.source.name)
            assertThat(record.get(Tables.ENTRY.SOURCEID)).isEqualTo(expected.sourceId)
            assertThat(record.get(Tables.ENTRY.DIAGNOSISYEAR)).isEqualTo(expected.diagnosisYear)
            assertThat(record.get(Tables.ENTRY.AGEATDIAGNOSIS)).isEqualTo(expected.ageAtDiagnosis)
            assertThat(record.get(Tables.ENTRY.SEX)).isEqualTo(expected.sex.name)

            compareSurvivalMeasurements(entryId, expected)
            comparePriorTumors(entryId, expected)
            comparePrimaryDiagnosis(entryId, expected)
            compareMetastaticDiagnosis(entryId, expected)
            compareWhoAssessments(entryId, expected)
            compareAsaAssessments(entryId, expected)
            compareMolecularResults(entryId, expected)
            compareLabMeasurements(entryId, expected)
            compareTreatmentEpisodes(entryId, expected)
        }
    }
    
    private fun compareSurvivalMeasurements(entryId: Int, expected: ReferenceEntry) {
        val survivalMeasurementRecords =
            dslContext.selectFrom(Tables.SURVIVALMEASUREMENT).where(Tables.SURVIVALMEASUREMENT.ENTRYID.eq(entryId)).fetch()
        
        compare(survivalMeasurementRecords.first(), expected.latestSurvivalMeasurement)
    }

    private fun compare(record: SurvivalmeasurementRecord, expected: SurvivalMeasurement) {
        val table = Tables.SURVIVALMEASUREMENT
        assertThat(record.get(table.DAYSSINCEDIAGNOSIS)).isEqualTo(expected.daysSinceDiagnosis)
        assertThat(record.get(table.ISALIVE)).isEqualTo(expected.isAlive)
    }

    private fun comparePriorTumors(entryId: Int, expected: ReferenceEntry) {
        val priorTumorsRecords = dslContext.selectFrom(Tables.PRIORTUMOR).where(Tables.PRIORTUMOR.ENTRYID.eq(entryId)).fetch()
        assertThat(priorTumorsRecords.size).isEqualTo(expected.priorTumors.size)
        
        expected.priorTumors.mapIndexed { index, priorTumor ->
            val priorTumorRecord = dslContext.selectFrom(Tables.PRIORTUMOR)
                .where(Tables.PRIORTUMOR.ID.eq(index + 1).and(Tables.PRIORTUMOR.ENTRYID.eq(entryId))).fetchOne()
            
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

    private fun comparePrimaryDiagnosis(entryId: Int, expected: ReferenceEntry) {
        val primaryDiagnosisRecord =
            dslContext.selectFrom(Tables.PRIMARYDIAGNOSIS).where(Tables.PRIMARYDIAGNOSIS.ENTRYID.eq(entryId)).fetchOne()
        
        compare(primaryDiagnosisRecord!!, expected.primaryDiagnosis)
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

    private fun compareMetastaticDiagnosis(entryId: Int, expected: ReferenceEntry) {
        val metastaticDiagnosisRecord =
            dslContext.selectFrom(Tables.METASTATICDIAGNOSIS).where(Tables.METASTATICDIAGNOSIS.ENTRYID.eq(entryId)).fetchOne()
        
        compare(metastaticDiagnosisRecord!!, expected.metastaticDiagnosis)
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
            
            compare(metastasisRecord!!, metastasis)
        }
    }

    private fun compare(record: MetastasisRecord, expected: Metastasis) {
        val table = Tables.METASTASIS
        assertThat(record.get(table.DAYSSINCEDIAGNOSIS) ?: null).isEqualTo(expected.daysSinceDiagnosis)
        assertThat(record.get(table.LOCATION)).isEqualTo(expected.location.name)
        assertThat(record.get(table.ISLINKEDTOPROGRESSION) ?: null).isEqualTo(expected.isLinkedToProgression)
    }
    
    private fun compareWhoAssessments(entryId: Int, expected: ReferenceEntry) {
        val whoAssessments = dslContext.selectFrom(Tables.WHOASSESSMENT).where(Tables.WHOASSESSMENT.ENTRYID.eq(entryId)).fetch()
        assertThat(whoAssessments.size).isEqualTo(expected.whoAssessments.size)
        
        expected.whoAssessments.mapIndexed { index, whoAssessment ->
            val whoAssessmentRecord = dslContext.selectFrom(Tables.WHOASSESSMENT)
                .where(Tables.WHOASSESSMENT.ID.eq(index + 1).and(Tables.WHOASSESSMENT.ENTRYID.eq(entryId))).fetchOne()
        
            compare(whoAssessmentRecord!!, whoAssessment)
        }
    }

    private fun compare(record: WhoassessmentRecord, expected: WhoAssessment) {
        val table = Tables.WHOASSESSMENT
        assertThat(record.get(table.DAYSSINCEDIAGNOSIS)).isEqualTo(expected.daysSinceDiagnosis)
        assertThat(record.get(table.WHOSTATUS)).isEqualTo(expected.whoStatus)
    }

    private fun compareAsaAssessments(entryId: Int, expectedEntry: ReferenceEntry) {
        // TODO (KD): Implement
    }

    private fun compareMolecularResults(entryId: Int, expectedEntry: ReferenceEntry) {
        // TODO (KD): Implement
    }

    private fun compareLabMeasurements(entryId: Int, expectedEntry: ReferenceEntry) {
        // TODO (KD): Implement
    }

    private fun compareTreatmentEpisodes(entryId: Int, expectedEntry: ReferenceEntry) {
        // TODO (KD): Implement
    }
}