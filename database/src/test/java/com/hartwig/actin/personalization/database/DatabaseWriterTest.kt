package com.hartwig.actin.personalization.database

import org.testcontainers.containers.MySQLContainer

class MySQLTestContainer : MySQLContainer<MySQLTestContainer>("mysql:8.4.4")

class DatabaseWriterTest {

//    private val expectedDiagnosisYear = 2000
//
//    private val mysqlContainer = MySQLTestContainer().apply {
//        withDatabaseName("testdb")
//        withUsername("test")
//        withInitScript("generate_database.sql")
//        withTmpFs(mapOf("/var/lib/mysql" to "rw"))
//        start()
//    }
//
//    private val connection: Connection = DriverManager.getConnection(
//        mysqlContainer.jdbcUrl, mysqlContainer.username, mysqlContainer.password
//    )
//
//    private val dslContext: DSLContext = DSL.using(connection, SQLDialect.MYSQL)
//
//    private val writer = DatabaseWriter(dslContext, connection)
//
//    @Test
//    fun `Should verify that database and all tables are created properly`() {
//        val tables = dslContext.meta().schemas.flatMap { it.tables }.map { it.name }.toSet()
//        assertThat(tables).containsAll(DefaultSchema.DEFAULT_SCHEMA.tables.map { it.name })
//    }
//
//    @Test
//    fun `Should insert patients with empty data`() {
//        val records = listOf(TestReferencePatientFactory.emptyReferencePatient())
//
//        println(records)
//        writer.writeAllToDb(records)
//        compare(records)
//    }
//
//    @Test
//    fun `Should insert patients with minimum data`() {
//        val records = listOf(TestReferencePatientFactory.minimalReferencePatient())
//
//        println(records)
//        writer.writeAllToDb(records)
//        compare(records)
//    }
//
//    @Test
//    fun `Should insert data in all tables for multiple reference patients`() {
//        val records = listOf(
//            TestReferencePatientFactory.emptyReferencePatient(),
//            TestReferencePatientFactory.minimalReferencePatient(),
//            TestReferencePatientFactory.exhaustiveReferencePatient()
//        )
//
//        println(records)
//        writer.writeAllToDb(records)
//        compare(records)
//    }
//
//    private fun compare(expectedPatients: List<ReferencePatient>) {
//        val patientRecords = dslContext.selectFrom(Tables.PATIENT).fetch()
//        assertThat(patientRecords.size).isEqualTo(expectedPatients.size)
//        expectedPatients.mapIndexed { index, patient ->
//            val patientRecord = dslContext.selectFrom(Tables.PATIENT).where(Tables.PATIENT.ID.eq(index + 1)).fetchOne()
//            assertThat(patientRecord).isNotNull()
//
//            assertThat(patientRecord!!.get(Tables.PATIENT.SEX)).isEqualTo(patient.sex.name)
//            val patientId = patientRecord.get(Tables.PATIENT.ID)
//
//            val tumorRecords = dslContext.selectFrom(Tables.TUMOR).where(Tables.TUMOR.PATIENTID.eq(patientId)).fetch()
//            assertThat(tumorRecords.size).isEqualTo(patient.tumors.size)
//
//            patient.tumors.firstOrNull { it.diagnosisYear == expectedDiagnosisYear }?.let { expectedTumor ->
//                val expectedTumorRecord = dslContext.selectFrom(Tables.TUMOR)
//                    .where(Tables.TUMOR.PATIENTID.eq(patientId).and(Tables.TUMOR.DIAGNOSISYEAR.eq(expectedDiagnosisYear))).fetchOne()
//                assertThat(expectedTumorRecord).isNotNull()
//                compareTumor(expectedTumorRecord!!, expectedTumor)
//            }
//        }
//    }
//
//    private fun compareTumor(record: TumorRecord, expectedTumor: Tumor) {
//        assertThat(record.get(Tables.TUMOR.DIAGNOSISYEAR)).isEqualTo(expectedTumor.diagnosisYear)
//        assertThat(record.get(Tables.TUMOR.AGEATDIAGNOSIS)).isEqualTo(expectedTumor.ageAtDiagnosis)
//        val tumorId = record.get(Tables.TUMOR.ID)
//
//        val survivalMeasureRecords =
//            dslContext.selectFrom(Tables.SURVIVALMEASURE).where(Tables.SURVIVALMEASURE.TUMORID.eq(tumorId)).fetchOne()
//
//        assertThat(survivalMeasureRecords).isNotNull()
//        compare(survivalMeasureRecords!!, expectedTumor.latestSurvivalStatus)
//
//        val priorTumorsRecords = dslContext.selectFrom(Tables.PRIORTUMOR).where(Tables.PRIORTUMOR.TUMORID.eq(tumorId)).fetch()
//        assertThat(priorTumorsRecords.size).isEqualTo(expectedTumor.priorTumors.size)
//        expectedTumor.priorTumors.mapIndexed { index, priorTumor ->
//            val priorTummorRecord = dslContext.selectFrom(Tables.PRIORTUMOR)
//                .where(Tables.PRIORTUMOR.ID.eq(index + 1).and(Tables.PRIORTUMOR.TUMORID.eq(tumorId))).fetchOne()
//            assertThat(priorTummorRecord).isNotNull()
//            comparePriorTumor(priorTummorRecord!!, priorTumor)
//        }
//
//        val primaryDiagnosisRecord =
//            dslContext.selectFrom(Tables.PRIMARYDIAGNOSIS).where(Tables.PRIMARYDIAGNOSIS.TUMORID.eq(tumorId)).fetchOne()
//        assertThat(primaryDiagnosisRecord).isNotNull()
//        compare(primaryDiagnosisRecord!!, expectedTumor.primaryDiagnosis)
//
//        val metastaticDiagnosisRecord =
//            dslContext.selectFrom(Tables.METASTATICDIAGNOSIS).where(Tables.METASTATICDIAGNOSIS.TUMORID.eq(tumorId)).fetchOne()
//        assertThat(metastaticDiagnosisRecord).isNotNull()
//        compare(metastaticDiagnosisRecord!!, expectedTumor.metastaticDiagnosis)
//        // TODO validate whoAssessments
//        // TODO validate asaAssessments
//        // TODO validate comorbidityAssessments
//        // TODO validate molecularResults
//        // TODO validate labMeasurements
//        // TODO validate gastroenterologyResections
//        // TODO validate primarySurgeries
//        // TODO validate metastaticSurgeries
//
////        val hipecTreatmentRecords = dslContext.selectFrom(Tables.HIPECTREATMENT).where(Tables.HIPECTREATMENT.TUMORID.eq(tumorId)).fetchOne()
////        assertThat(hipecTreatmentRecords).isNotNull()
////        // TODO compare hipecTreatment
//
//        // TODO validate primaryRadiotherapies
//        // TODO validate metastaticRadiotherapies
//        // TODO validate systemicTreatments
//        // TODO validate responseMeasures
//        // TODO validate progressionMeasures
//
//    }
//
//    private fun comparePriorTumor(record: PriortumorRecord, expected: PriorTumor) {
//        assertThat(record.get(Tables.PRIORTUMOR.DAYSBEFOREDIAGNOSIS) ?: null).isEqualTo(expected.daysBeforeDiagnosis)
//        assertThat(record.get(Tables.PRIORTUMOR.PRIMARYTUMORTYPE)).isEqualTo(expected.primaryTumorType.name)
//        assertThat(record.get(Tables.PRIORTUMOR.PRIMARYTUMORLOCATION)).isEqualTo(expected.primaryTumorLocation.name)
//        assertThat(record.get(Tables.PRIORTUMOR.PRIMARYTUMORLOCATIONCATEGORY)).isEqualTo(expected.primaryTumorLocationCategory.name)
//        assertThat(record.get(Tables.PRIORTUMOR.PRIMARYTUMORSTAGE ?: null)).isEqualTo(expected.primaryTumorStage?.name)
//        // TODO JSON assertThat(record.get(Tables.PRIORTUMOR.SYSTEMICDRUGSRECEIVED))
//    }
//
//    private fun compare(record: PrimarydiagnosisRecord, expected: PrimaryDiagnosis) {
//        val table = Tables.PRIMARYDIAGNOSIS
//        assertThat(record.get(table.BASISOFDIAGNOSIS) ?: null).isEqualTo(expected.basisOfDiagnosis.name)
//        assertThat(record.get(table.HASDOUBLEPRIMARYTUMOR) ?: null).isEqualTo(expected.hasDoublePrimaryTumor)
//        assertThat(record.get(table.PRIMARYTUMORTYPE) ?: null).isEqualTo(expected.primaryTumorType.name)
//        assertThat(record.get(table.PRIMARYTUMORLOCATION) ?: null).isEqualTo(expected.primaryTumorLocation.name)
//        assertThat(record.get(table.DIFFERENTIATIONGRADE) ?: null).isEqualTo(expected.differentiationGrade?.name)
//
//        compare(record.get(table.CLINICALTNMCLASSIFICATION), expected.clinicalTnmClassification)
//        compare(record.get(table.PATHOLOGICALTNMCLASSIFICATION), expected.pathologicalTnmClassification)
//        assertThat(record.get(table.CLINICALTUMORSTAGE) ?: null).isEqualTo(expected.clinicalTumorStage.name)
//        assertThat(record.get(table.PATHOLOGICALTUMORSTAGE) ?: null).isEqualTo(expected.pathologicalTumorStage.name)
//        assertThat(record.get(table.INVESTIGATEDLYMPHNODESCOUNT) ?: null).isEqualTo(expected.investigatedLymphNodesCount)
//        assertThat(record.get(table.POSITIVELYMPHNODESCOUNT) ?: null).isEqualTo(expected.positiveLymphNodesCount)
//
//        assertThat(record.get(table.VENOUSINVASIONDESCRIPTION) ?: null).isEqualTo(expected.venousInvasionDescription?.name)
//        assertThat(record.get(table.LYMPHATICINVASIONCATEGORY) ?: null).isEqualTo(expected.lymphaticInvasionCategory?.name)
//        assertThat(record.get(table.EXTRAMURALINVASIONCATEGORY) ?: null).isEqualTo(expected.extraMuralInvasionCategory?.name)
//        assertThat(record.get(table.TUMORREGRESSION) ?: null).isEqualTo(expected.tumorRegression?.name)
//
//        assertThat(record.get(table.SIDEDNESS) ?: null).isEqualTo(expected.sidedness?.name)
//        assertThat(record.get(table.PRESENTEDWITHILEUS) ?: null).isEqualTo(expected.presentedWithIleus)
//        assertThat(record.get(table.PRESENTEDWITHPERFORATION) ?: null).isEqualTo(expected.presentedWithPerforation)
//
//        assertThat(record.get(table.ANORECTALVERGEDISTANCECATEGORY) ?: null).isEqualTo(expected.anorectalVergeDistanceCategory?.name)
//        assertThat(record.get(table.MESORECTALFASCIAISCLEAR) ?: null).isEqualTo(expected.mesorectalFasciaIsClear)
//        assertThat(record.get(table.DISTANCETOMESORECTALFASCIAMM) ?: null).isEqualTo(expected.distanceToMesorectalFasciaMm)
//    }
//
//    private fun compare(record: MetastaticdiagnosisRecord, expected: MetastaticDiagnosis) {
//        val table = Tables.METASTATICDIAGNOSIS
//        assertThat(record.get(table.DISTANTMETASTASESDETECTIONSTATUS) ?: null).isEqualTo(expected.distantMetastasesDetectionStatus.name)
//        assertThat(record.get(table.NUMBEROFLIVERMETASTASES) ?: null).isEqualTo(expected.numberOfLiverMetastases?.name)
//        assertThat(record.get(table.MAXIMUMSIZEOFLIVERMETASTASISMM) ?: null).isEqualTo(expected.maximumSizeOfLiverMetastasisMm)
//        assertThat(record.get(table.INVESTIGATEDLYMPHNODESCOUNT) ?: null).isEqualTo(expected.investigatedLymphNodesCount)
//        assertThat(record.get(table.POSITIVELYMPHNODESCOUNT) ?: null).isEqualTo(expected.positiveLymphNodesCount)
//        // TODO validate metastases
//    }
//
//    private fun compare(json: JSON?, expected: TnmClassification?) {
//        if (expected != null) {
//            assertThat(Json.decodeFromString<TnmClassification>(json!!.data())).isEqualTo(expected)
//        } else {
//            assertThat(json).isNull()
//        }
//    }
//
//    private fun compare(record: SurvivalmeasureRecord, survivalMeasure: SurvivalMeasure) {
//        assertThat(record.get(Tables.SURVIVALMEASURE.DAYSSINCEDIAGNOSIS)).isEqualTo(survivalMeasure.daysSinceDiagnosis)
//        assertThat(record.get(Tables.SURVIVALMEASURE.ISALIVE)).isEqualTo(survivalMeasure.isAlive)
//    }

}