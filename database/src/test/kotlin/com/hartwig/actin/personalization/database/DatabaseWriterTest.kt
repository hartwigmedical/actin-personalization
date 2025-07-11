package com.hartwig.actin.personalization.database

import com.hartwig.actin.personalization.database.tables.records.AsaassessmentRecord
import com.hartwig.actin.personalization.database.tables.records.ComorbidityassessmentRecord
import com.hartwig.actin.personalization.database.tables.records.LabmeasurementRecord
import com.hartwig.actin.personalization.database.tables.records.MetastasisRecord
import com.hartwig.actin.personalization.database.tables.records.MetastaticdiagnosisRecord
import com.hartwig.actin.personalization.database.tables.records.MolecularresultRecord
import com.hartwig.actin.personalization.database.tables.records.PrimarydiagnosisRecord
import com.hartwig.actin.personalization.database.tables.records.PriortumorRecord
import com.hartwig.actin.personalization.database.tables.records.SurvivalmeasurementRecord
import com.hartwig.actin.personalization.database.tables.records.TreatmentepisodeRecord
import com.hartwig.actin.personalization.database.tables.records.WhoassessmentRecord
import com.hartwig.actin.personalization.datamodel.ReferenceEntry
import com.hartwig.actin.personalization.datamodel.TestReferenceEntryFactory
import com.hartwig.actin.personalization.datamodel.assessment.AsaAssessment
import com.hartwig.actin.personalization.datamodel.assessment.ComorbidityAssessment
import com.hartwig.actin.personalization.datamodel.assessment.LabMeasurement
import com.hartwig.actin.personalization.datamodel.assessment.MolecularResult
import com.hartwig.actin.personalization.datamodel.assessment.WhoAssessment
import com.hartwig.actin.personalization.datamodel.diagnosis.Metastasis
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastaticDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.PrimaryDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.PriorTumor
import com.hartwig.actin.personalization.datamodel.outcome.SurvivalMeasurement
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentEpisode
import org.assertj.core.api.Assertions.assertThat
import org.jooq.Record
import org.jooq.SQLDialect
import org.jooq.Table
import org.jooq.TableField
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
    private val dsl = DSL.using(connection, SQLDialect.MYSQL)
    private val writer = DatabaseWriter(dsl, connection)

    @Test
    fun `Should verify that database and all tables are created properly`() {
        val tables = dsl.meta().schemas.flatMap { it.tables }.map { it.name }.toSet()
        assertThat(tables).containsAll(DefaultSchema.DEFAULT_SCHEMA.tables.map { it.name })
    }

    @Test
    fun `Should insert empty reference entry`() {
        val entries = listOf(TestReferenceEntryFactory.empty())

        writer.writeAllToDb(entries)
        compareEntries(entries)
    }

    @Test
    fun `Should insert minimal reference entry`() {
        val records = listOf(TestReferenceEntryFactory.minimal())

        writer.writeAllToDb(records)
        compareEntries(records)
    }

    @Test
    fun `Should insert exhaustive reference entry`() {
        val entries = listOf(TestReferenceEntryFactory.exhaustive())

        writer.writeAllToDb(entries)
        compareEntries(entries)
    }

    private fun compareEntries(expectedEntries: List<ReferenceEntry>) {
        val referenceEntries = dsl.selectFrom(Tables.ENTRY).fetch()
        assertThat(referenceEntries.size).isEqualTo(expectedEntries.size)
        expectedEntries.mapIndexed { index, expected ->
            val entryId = index + 1
            val record = dsl.selectFrom(Tables.ENTRY).where(Tables.ENTRY.ID.eq(entryId)).fetchOne()

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
            compareComorbidityAssessments(entryId, expected)
            compareMolecularResults(entryId, expected)
            compareLabMeasurements(entryId, expected)
            compareTreatmentEpisodes(entryId, expected)
        }
    }

    private fun compareSurvivalMeasurements(entryId: Int, expected: ReferenceEntry) {
        compareListOfElements(
            entryId,
            listOf(expected.latestSurvivalMeasurement),
            Tables.SURVIVALMEASUREMENT,
            Tables.SURVIVALMEASUREMENT.ENTRYID,
            Tables.SURVIVALMEASUREMENT.ENTRYID,
            ::compareSurvivalMeasurement
        )
    }

    private fun comparePriorTumors(entryId: Int, expected: ReferenceEntry) {
        compareListOfElements(
            entryId,
            expected.priorTumors,
            Tables.PRIORTUMOR,
            Tables.PRIORTUMOR.ID,
            Tables.PRIORTUMOR.ENTRYID,
            ::comparePriorTumor
        )
    }

    private fun comparePrimaryDiagnosis(entryId: Int, expected: ReferenceEntry) {
        compareListOfElements(
            entryId,
            listOf(expected.primaryDiagnosis),
            Tables.PRIMARYDIAGNOSIS,
            Tables.PRIMARYDIAGNOSIS.ENTRYID,
            Tables.PRIMARYDIAGNOSIS.ENTRYID,
            ::comparePrimaryDiagnosis
        )
    }

    private fun compareMetastaticDiagnosis(entryId: Int, expected: ReferenceEntry) {
        compareListOfElements(
            entryId,
            listOf(expected.metastaticDiagnosis),
            Tables.METASTATICDIAGNOSIS,
            Tables.METASTATICDIAGNOSIS.ID,
            Tables.METASTATICDIAGNOSIS.ENTRYID,
            ::compareMetastaticDiagnosis
        )
    }

    private fun compareWhoAssessments(entryId: Int, expected: ReferenceEntry) {
        compareListOfElements(
            entryId,
            expected.whoAssessments,
            Tables.WHOASSESSMENT,
            Tables.WHOASSESSMENT.ID,
            Tables.WHOASSESSMENT.ENTRYID,
            ::compareWhoAssessment
        )
    }

    private fun compareAsaAssessments(entryId: Int, expected: ReferenceEntry) {
        compareListOfElements(
            entryId,
            expected.asaAssessments,
            Tables.ASAASSESSMENT,
            Tables.ASAASSESSMENT.ID,
            Tables.ASAASSESSMENT.ENTRYID,
            ::compareAsaAssessment
        )
    }

    private fun compareComorbidityAssessments(entryId: Int, expected: ReferenceEntry) {
        compareListOfElements(
            entryId,
            expected.comorbidityAssessments,
            Tables.COMORBIDITYASSESSMENT,
            Tables.COMORBIDITYASSESSMENT.ID,
            Tables.COMORBIDITYASSESSMENT.ENTRYID,
            ::compareComorbidityAssessment
        )
    }

    private fun compareMolecularResults(entryId: Int, expected: ReferenceEntry) {
        compareListOfElements(
            entryId,
            expected.molecularResults,
            Tables.MOLECULARRESULT,
            Tables.MOLECULARRESULT.ID,
            Tables.MOLECULARRESULT.ENTRYID,
            ::compareMolecularResult
        )
    }

    private fun compareLabMeasurements(entryId: Int, expected: ReferenceEntry) {
        compareListOfElements(
            entryId,
            expected.labMeasurements,
            Tables.LABMEASUREMENT,
            Tables.LABMEASUREMENT.ID,
            Tables.LABMEASUREMENT.ENTRYID,
            ::compareLabMeasurements
        )
    }

    private fun compareTreatmentEpisodes(entryId: Int, expected: ReferenceEntry) {
        compareListOfElements(
            entryId,
            expected.treatmentEpisodes,
            Tables.TREATMENTEPISODE,
            Tables.TREATMENTEPISODE.ID,
            Tables.TREATMENTEPISODE.ENTRYID,
            ::compareTreatmentEpisodes
        )
    }

    private fun compareSurvivalMeasurement(record: SurvivalmeasurementRecord, expected: SurvivalMeasurement) {
        val table = Tables.SURVIVALMEASUREMENT
        assertThat(record.get(table.DAYSSINCEDIAGNOSIS)).isEqualTo(expected.daysSinceDiagnosis)
        assertThat(record.get(table.ISALIVE)).isEqualTo(expected.isAlive)
    }

    private fun comparePriorTumor(record: PriortumorRecord, expected: PriorTumor) {
        val table = Tables.PRIORTUMOR
        assertThat(record.get(table.DAYSBEFOREDIAGNOSIS) ?: null).isEqualTo(expected.daysBeforeDiagnosis)
        assertThat(record.get(table.PRIMARYTUMORTYPE)).isEqualTo(expected.primaryTumorType.name)
        assertThat(record.get(table.PRIMARYTUMORLOCATION)).isEqualTo(expected.primaryTumorLocation.name)
        assertThat(record.get(table.PRIMARYTUMORLOCATIONCATEGORY)).isEqualTo(expected.primaryTumorLocationCategory.name)
        assertThat(record.get(table.PRIMARYTUMORSTAGE ?: null)).isEqualTo(expected.primaryTumorStage?.name)
        assertThat(record.get(table.SYSTEMICDRUGSRECEIVED ?: null)).isEqualTo(DatabaseWriter.concat(expected.systemicDrugsReceived))
    }

    private fun comparePrimaryDiagnosis(record: PrimarydiagnosisRecord, expected: PrimaryDiagnosis) {
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
        assertThat(record.get(table.PATHOLOGICALTNMT) ?: null).isEqualTo(expected.pathologicalTnmClassification?.tnmT?.name)
        assertThat(record.get(table.PATHOLOGICALTNMN) ?: null).isEqualTo(expected.pathologicalTnmClassification?.tnmN?.name)
        assertThat(record.get(table.PATHOLOGICALTNMM) ?: null).isEqualTo(expected.pathologicalTnmClassification?.tnmM?.name)
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

    private fun compareMetastaticDiagnosis(record: MetastaticdiagnosisRecord, expected: MetastaticDiagnosis) {
        val table = Tables.METASTATICDIAGNOSIS
        assertThat(record.get(table.ISMETACHRONOUS)).isEqualTo(expected.isMetachronous)
        assertThat(record.get(table.NUMBEROFLIVERMETASTASES) ?: null).isEqualTo(expected.numberOfLiverMetastases?.name)
        assertThat(record.get(table.MAXIMUMSIZEOFLIVERMETASTASISMM) ?: null).isEqualTo(expected.maximumSizeOfLiverMetastasisMm)
        assertThat(record.get(table.INVESTIGATEDLYMPHNODESCOUNT) ?: null).isEqualTo(expected.investigatedLymphNodesCount)
        assertThat(record.get(table.POSITIVELYMPHNODESCOUNT) ?: null).isEqualTo(expected.positiveLymphNodesCount)

        expected.metastases.mapIndexed { index, metastasis ->
            val metastasisRecord = dsl.selectFrom(Tables.METASTASIS)
                .where(Tables.METASTASIS.ID.eq(index + 1).and(Tables.METASTASIS.METASTATICDIAGNOSISID.eq(record.get(table.ID)))).fetchOne()

            compareMetastasis(metastasisRecord!!, metastasis)
        }
    }

    private fun compareMetastasis(record: MetastasisRecord, expected: Metastasis) {
        val table = Tables.METASTASIS
        assertThat(record.get(table.DAYSSINCEDIAGNOSIS) ?: null).isEqualTo(expected.daysSinceDiagnosis)
        assertThat(record.get(table.LOCATION)).isEqualTo(expected.location.name)
        assertThat(record.get(table.ISLINKEDTOPROGRESSION) ?: null).isEqualTo(expected.isLinkedToProgression)
    }

    private fun compareWhoAssessment(record: WhoassessmentRecord, expected: WhoAssessment) {
        val table = Tables.WHOASSESSMENT
        assertThat(record.get(table.DAYSSINCEDIAGNOSIS)).isEqualTo(expected.daysSinceDiagnosis)
        assertThat(record.get(table.WHOSTATUS)).isEqualTo(expected.whoStatus)
    }

    private fun compareAsaAssessment(record: AsaassessmentRecord, expected: AsaAssessment) {
        val table = Tables.ASAASSESSMENT
        assertThat(record.get(table.DAYSSINCEDIAGNOSIS)).isEqualTo(expected.daysSinceDiagnosis)
        assertThat(record.get(table.ASACLASSIFICATION)).isEqualTo(expected.classification.name)
    }

    private fun compareComorbidityAssessment(record: ComorbidityassessmentRecord, expected: ComorbidityAssessment) {
        val table = Tables.COMORBIDITYASSESSMENT

        assertThat(record.get(table.DAYSSINCEDIAGNOSIS)).isEqualTo(expected.daysSinceDiagnosis)

        assertThat(record.get(table.CHARLSONCOMORBIDITYINDEX)).isEqualTo(expected.charlsonComorbidityIndex)

        assertThat(record.get(table.HASAIDS)).isEqualTo(expected.hasAids)
        assertThat(record.get(table.HASCONGESTIVEHEARTFAILURE)).isEqualTo(expected.hasCongestiveHeartFailure)
        assertThat(record.get(table.HASCOLLAGENOSIS)).isEqualTo(expected.hasCollagenosis)
        assertThat(record.get(table.HASCOPD)).isEqualTo(expected.hasCopd)
        assertThat(record.get(table.HASCEREBROVASCULARDISEASE)).isEqualTo(expected.hasCerebrovascularDisease)
        assertThat(record.get(table.HASDEMENTIA)).isEqualTo(expected.hasDementia)
        assertThat(record.get(table.HASDIABETESMELLITUS)).isEqualTo(expected.hasDiabetesMellitus)
        assertThat(record.get(table.HASDIABETESMELLITUSWITHENDORGANDAMAGE)).isEqualTo(expected.hasDiabetesMellitusWithEndOrganDamage)
        assertThat(record.get(table.HASOTHERMALIGNANCY)).isEqualTo(expected.hasOtherMalignancy)
        assertThat(record.get(table.HASOTHERMETASTATICSOLIDTUMOR)).isEqualTo(expected.hasOtherMetastaticSolidTumor)
        assertThat(record.get(table.HASMYOCARDIALINFARCT)).isEqualTo(expected.hasMyocardialInfarct)
        assertThat(record.get(table.HASMILDLIVERDISEASE)).isEqualTo(expected.hasMildLiverDisease)
        assertThat(record.get(table.HASHEMIPLEGIAORPARAPLEGIA)).isEqualTo(expected.hasHemiplegiaOrParaplegia)
        assertThat(record.get(table.HASPERIPHERALVASCULARDISEASE)).isEqualTo(expected.hasPeripheralVascularDisease)
        assertThat(record.get(table.HASRENALDISEASE)).isEqualTo(expected.hasRenalDisease)
        assertThat(record.get(table.HASLIVERDISEASE)).isEqualTo(expected.hasLiverDisease)
        assertThat(record.get(table.HASULCERDISEASE)).isEqualTo(expected.hasUlcerDisease)
    }

    private fun compareMolecularResult(record: MolecularresultRecord, expected: MolecularResult) {
        val table = Tables.MOLECULARRESULT

        assertThat(record.get(table.DAYSSINCEDIAGNOSIS)).isEqualTo(expected.daysSinceDiagnosis)

        assertThat(record.get(table.HASMSI) ?: null).isEqualTo(expected.hasMsi)
        assertThat(record.get(table.HASBRAFMUTATION) ?: null).isEqualTo(expected.hasBrafMutation)
        assertThat(record.get(table.HASBRAFV600EMUTATION) ?: null).isEqualTo(expected.hasBrafV600EMutation)
        assertThat(record.get(table.HASRASMUTATION) ?: null).isEqualTo(expected.hasRasMutation)
        assertThat(record.get(table.HASKRASG12CMUTATION) ?: null).isEqualTo(expected.hasKrasG12CMutation)
    }

    private fun compareLabMeasurements(record: LabmeasurementRecord, expected: LabMeasurement) {
        val table = Tables.LABMEASUREMENT

        assertThat(record.get(table.DAYSSINCEDIAGNOSIS)).isEqualTo(expected.daysSinceDiagnosis)

        assertThat(record.get(table.NAME)).isEqualTo(expected.name.name)
        assertThat(record.get(table.VALUE)).isEqualTo(expected.value)
        assertThat(record.get(table.UNIT)).isEqualTo(expected.unit.name)
        assertThat(record.get(table.ISPRESURGICAL) ?: null).isEqualTo(expected.isPreSurgical)
        assertThat(record.get(table.ISPOSTSURGICAL) ?: null).isEqualTo(expected.isPostSurgical)
    }

    private fun compareTreatmentEpisodes(record: TreatmentepisodeRecord, expected: TreatmentEpisode) {
        val table = Tables.TREATMENTEPISODE
        assertThat(record.get(table.METASTATICPRESENCE)).isEqualTo(expected.metastaticPresence.name)
        assertThat(record.get(table.REASONREFRAINMENTFROMTREATMENT)).isEqualTo(expected.reasonRefrainmentFromTreatment.name)
    }
    
    private fun <R, T> compareListOfElements(
        entryId: Int,
        expected: List<T>,
        table: Table<R>,
        idField: TableField<R, Int>,
        entryIdField: TableField<R, Int>,
        compareFunction: (R, T) -> Unit
    ) where R : Record {
        val records = dsl.selectFrom(table).where(entryIdField.eq(entryId)).fetch()

        assertThat(records.size).isEqualTo(expected.size)

        expected.mapIndexed { index, objectX ->
            val record = dsl.selectFrom(table).where(idField.eq(index + 1).and(entryIdField.eq(entryId))).fetchOne()

            compareFunction.invoke(record!!, objectX)
        }
    }
}