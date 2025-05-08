package com.hartwig.actin.personalization.database

import com.hartwig.actin.personalization.database.datamodel.ReferenceRecord
import com.hartwig.actin.personalization.datamodel.ReferencePatient
import com.hartwig.actin.personalization.datamodel.Tumor
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastaticDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocation
import com.hartwig.actin.personalization.datamodel.treatment.Drug
import com.hartwig.actin.personalization.datamodel.treatment.SystemicTreatment
import com.hartwig.actin.personalization.datamodel.treatment.SystemicTreatmentScheme
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentEpisode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.Table
import org.jooq.TableRecord
import org.jooq.impl.DSL
import java.sql.DriverManager

private typealias IndexedList<T> = List<Pair<Int, T>>

class DatabaseWriter(private val context: DSLContext, private val connection: java.sql.Connection) {

    fun writeAllToDb(referencePatients: List<ReferencePatient>) {
        connection.autoCommit = false
        clearAll()

        val indexedReferencePatients = writeReferencePatients(referencePatients)
        val indexedTumors = writeTumors(indexedReferencePatients)
        writeReferenceRecords(ReferenceRecordFactory.create(referencePatients, indexedTumors))
        writeRecords("survivalMeasurement", indexedTumors, ::survivalMeasurementFromTumor)
        writeRecords("priorTumor", indexedTumors, ::priorTumorFromTumor)
        writeRecords("primaryDiagnosis", indexedTumors, ::primaryDiagnosisFromTumor)
        val indexedMetastaticDiagnoses = writeRecordsAndReturnIndexedList(
            "metastaticDiagnosis",
            indexedTumors,
            Tables.METASTATICDIAGNOSIS,
            ::metastaticDiagnosisFromTumor
        )
        writeRecords("metastasis", indexedMetastaticDiagnoses, ::metastasesFromMetastaticDiagnosis)
        writeRecords("whoAssessment", indexedTumors, ::whoAssessmentsFromTumor)
        writeRecords("asaAssessment", indexedTumors, ::asaAssessmentsFromTumor)
        writeRecords("comorbidityAssessment", indexedTumors, ::comorbidityAssessmentsFromTumor)
        writeRecords("molecularResult", indexedTumors, ::molecularResultsFromTumor)
        writeRecords("labMeasurement", indexedTumors, ::labMeasurementsFromTumor)

        val indexedTreatmentEpisodes = writeRecordsAndReturnIndexedList(
            "treatmentEpisode",
            indexedTumors,
            Tables.TREATMENTEPISODE,
            ::treatmentEpisodesFromTumor
        )
        writeRecords("gastroenterologyResection", indexedTreatmentEpisodes, ::gastroenterologyResectionsFromTreatmentEpisode)
        writeRecords("primarySurgery", indexedTreatmentEpisodes, ::primarySurgeriesFromTreatmentEpisode)
        writeRecords("metastaticSurgery", indexedTreatmentEpisodes, ::metastaticSurgeriesFromTreatmentEpisode)
        writeRecords("hipecTreatment", indexedTreatmentEpisodes, ::hipecTreatmentsFromTreatmentEpisode)
        writeRecords("primaryRadiotherapy", indexedTreatmentEpisodes, ::primaryRadiotherapiesFromTreatmentEpisode)
        writeRecords("metastaticRadiotherapy", indexedTreatmentEpisodes, ::metastaticRadiotherapiesFromTreatmentEpisode)

        val indexedSystemicTreatments = writeRecordsAndReturnIndexedList(
            "systemicTreatment",
            indexedTreatmentEpisodes,
            Tables.SYSTEMICTREATMENT,
            ::systemicTreatmentsFromTreatmentEpisode
        )

        val indexedSystemicTreatmentSchemes = writeRecordsAndReturnIndexedList(
            "systemicTreatmentScheme",
            indexedSystemicTreatments,
            Tables.SYSTEMICTREATMENTSCHEME,
            ::systemicTreatmentSchemesFromSystemicTreatment
        )

        writeRecords("systemicTreatmentDrug", indexedSystemicTreatmentSchemes, ::systemicTreatmentDrugFromSystemicTreatmentScheme)
        writeRecords("responseMeasure", indexedTreatmentEpisodes, ::responseMeasuresFromTreatmentEpisode)
        writeRecords("progressionMeasure", indexedTreatmentEpisodes, ::progressionMeasuresFromTreatmentEpisode)

        writeDrugReferences()
        writeTumorLocationReferences()

        connection.autoCommit = true
    }

    private fun clearAll() {
        LOGGER.info { " Clearing all patient data" }

        context.execute("SET FOREIGN_KEY_CHECKS = 0;")
        DefaultSchema.DEFAULT_SCHEMA.tables.forEach { context.truncate(it).execute() }
        connection.commit()
        context.execute("SET FOREIGN_KEY_CHECKS = 1;")
    }

    private fun writeReferencePatients(patientRecords: List<ReferencePatient>): IndexedList<ReferencePatient> {
        LOGGER.info { " Writing patient records" }
        val (indexedRecords, rows) = patientRecords.mapIndexed { index, record ->
            val patientId = index + 1
            val dbRecord = context.newRecord(Tables.PATIENT)
            dbRecord.from(record)
            dbRecord.set(Tables.PATIENT.ID, patientId)
            dbRecord.set(Tables.PATIENT.SOURCE, record.source.name)
            dbRecord.set(Tables.PATIENT.SEX, record.sex.name)
            Pair(patientId, record) to dbRecord
        }.unzip()

        insertRows(rows, "patient")
        return indexedRecords
    }

    private fun writeTumors(patientRecords: IndexedList<ReferencePatient>): IndexedList<Tumor> {
        LOGGER.info { " Writing tumor records" }

        val (indexedRecords, rows) = patientRecords
            .flatMap { (patientId, referencePatient) ->
                referencePatient.tumors.map { tumor -> patientId to tumor }
            }
            .withIndex()
            .map { (index, pair) ->
                val (patientId, tumor) = pair
                val tumorId = index + 1
                val dbRecord = context.newRecord(Tables.TUMOR)
                dbRecord.from(tumor)
                dbRecord.set(Tables.TUMOR.ID, tumorId)
                dbRecord.set(Tables.TUMOR.PATIENTID, patientId)
                Pair(tumorId, tumor) to dbRecord
            }.unzip()

        insertRows(rows, "tumor")
        return indexedRecords
    }

    private fun writeReferenceRecords(referenceRecords: List<ReferenceRecord>) {
        LOGGER.info { " Writing reference records" }
        
        val rows = referenceRecords.map { record ->
            val dbRecord = context.newRecord(Tables.REFERENCERECORD)
            dbRecord.from(record)
            dbRecord
        }

        insertRows(rows, "referenceRecord")
    }

    private fun <T, U : TableRecord<*>, V> writeRecordsAndReturnIndexedList(
        name: String, indexedRecords: IndexedList<T>, table: Table<U>, recordMapper: (Int, T) -> List<Pair<V, U>>
    ): IndexedList<V> {
        LOGGER.info { " Writing $name records" }
        val (outputEntries, rows) = indexedRecords.flatMap { (foreignKeyId, record) -> recordMapper(foreignKeyId, record) }
            .mapIndexed { index, (outputEntry: V, dbRecord: U) ->
                val id = index + 1
                dbRecord.set(table.field("id", Int::class.java), id)
                Pair(id, outputEntry) to dbRecord
            }
            .unzip()

        insertRows(rows, name)
        return outputEntries
    }

    private fun <T, U : TableRecord<*>> writeRecords(name: String, indexedRecords: IndexedList<T>, recordMapper: (Int, T) -> List<U>) {
        LOGGER.info { " Writing $name records" }
        val rows = indexedRecords.flatMap { (foreignKeyId, record) -> recordMapper(foreignKeyId, record) }
        insertRows(rows, name)
    }

    private fun insertRows(rows: List<TableRecord<*>>, name: String) {
        context.batchInsert(rows).execute()
        connection.commit()
        LOGGER.info { "  Inserted ${rows.size} $name records" }
    }
    
    private fun survivalMeasurementFromTumor(tumorId: Int, tumor: Tumor) =
        listOf(extractSimpleRecord(Tables.SURVIVALMEASUREMENT, tumor.latestSurvivalMeasurement, "tumorId", tumorId))

    private fun priorTumorFromTumor(tumorId: Int, tumor: Tumor) =
        tumor.priorTumors.map { priorTumor ->
            val dbRecord = context.newRecord(Tables.PRIORTUMOR)
            dbRecord.from(priorTumor)
            dbRecord.set(Tables.PRIORTUMOR.TUMORID, tumorId)
            dbRecord.set(Tables.PRIORTUMOR.SYSTEMICDRUGSRECEIVED, concat(priorTumor.systemicDrugsReceived))
            dbRecord
        }

    private fun primaryDiagnosisFromTumor(tumorId: Int, tumor: Tumor) =
        listOf(tumor.primaryDiagnosis).map { primaryDiagnosis ->
            val dbRecord = context.newRecord(Tables.PRIMARYDIAGNOSIS)
            dbRecord.from(primaryDiagnosis)
            dbRecord.set(Tables.PRIMARYDIAGNOSIS.TUMORID, tumorId)
            dbRecord.set(Tables.PRIMARYDIAGNOSIS.BASISOFDIAGNOSIS, primaryDiagnosis.basisOfDiagnosis.name)
            dbRecord.set(Tables.PRIMARYDIAGNOSIS.PRIMARYTUMORTYPE, primaryDiagnosis.primaryTumorType.name)
            dbRecord.set(Tables.PRIMARYDIAGNOSIS.PRIMARYTUMORLOCATION, primaryDiagnosis.primaryTumorLocation.name)
            dbRecord.set(Tables.PRIMARYDIAGNOSIS.ANORECTALVERGEDISTANCECATEGORY, primaryDiagnosis.anorectalVergeDistanceCategory?.name)
            dbRecord.set(Tables.PRIMARYDIAGNOSIS.DIFFERENTIATIONGRADE, primaryDiagnosis.differentiationGrade?.name)
            dbRecord.set(Tables.PRIMARYDIAGNOSIS.CLINICALTNMT, primaryDiagnosis.clinicalTnmClassification.tnmT?.name)
            dbRecord.set(Tables.PRIMARYDIAGNOSIS.CLINICALTNMN, primaryDiagnosis.clinicalTnmClassification.tnmN?.name)
            dbRecord.set(Tables.PRIMARYDIAGNOSIS.CLINICALTNMM, primaryDiagnosis.clinicalTnmClassification.tnmM?.name)
            dbRecord.set(Tables.PRIMARYDIAGNOSIS.PATHOLOGICALTNMT, primaryDiagnosis.pathologicalTnmClassification.tnmT?.name)
            dbRecord.set(Tables.PRIMARYDIAGNOSIS.PATHOLOGICALTNMN, primaryDiagnosis.pathologicalTnmClassification.tnmN?.name)
            dbRecord.set(Tables.PRIMARYDIAGNOSIS.PATHOLOGICALTNMM, primaryDiagnosis.pathologicalTnmClassification.tnmM?.name)
            dbRecord.set(Tables.PRIMARYDIAGNOSIS.CLINICALTUMORSTAGE, primaryDiagnosis.clinicalTumorStage.name)
            dbRecord.set(Tables.PRIMARYDIAGNOSIS.PATHOLOGICALTUMORSTAGE, primaryDiagnosis.pathologicalTumorStage.name)
            dbRecord.set(Tables.PRIMARYDIAGNOSIS.VENOUSINVASIONDESCRIPTION, primaryDiagnosis.venousInvasionDescription?.name)
            dbRecord.set(Tables.PRIMARYDIAGNOSIS.LYMPHATICINVASIONCATEGORY, primaryDiagnosis.lymphaticInvasionCategory?.name)
            dbRecord.set(Tables.PRIMARYDIAGNOSIS.EXTRAMURALINVASIONCATEGORY, primaryDiagnosis.extraMuralInvasionCategory?.name)
            dbRecord.set(Tables.PRIMARYDIAGNOSIS.TUMORREGRESSION, primaryDiagnosis.tumorRegression?.name)
            dbRecord
        }

    private fun metastaticDiagnosisFromTumor(tumorId: Int, tumor: Tumor) =
        tumor.metastaticDiagnosis.let {
            val dbRecord = context.newRecord(Tables.METASTATICDIAGNOSIS)
            with(tumor.metastaticDiagnosis) {
                dbRecord.from(this)
                dbRecord.set(Tables.METASTATICDIAGNOSIS.TUMORID, tumorId)
                dbRecord.set(Tables.METASTATICDIAGNOSIS.NUMBEROFLIVERMETASTASES, this.numberOfLiverMetastases?.name)
                listOf(this to dbRecord)
            }
        }

    private fun metastasesFromMetastaticDiagnosis(metastaticDiagnosisId: Int, metastaticDiagnosis: MetastaticDiagnosis) =
        metastaticDiagnosis.metastases.map { data ->
            val dbRecord = context.newRecord(Tables.METASTASIS)
            dbRecord.from(data)
            dbRecord.set(Tables.METASTASIS.METASTATICDIAGNOSISID, metastaticDiagnosisId)
            dbRecord.set(Tables.METASTASIS.LOCATION, data.location.name)
            dbRecord
        }

    private fun whoAssessmentsFromTumor(tumorId: Int, tumor: Tumor) =
        tumor.whoAssessments.map { data ->
            val dbRecord = context.newRecord(Tables.WHOASSESSMENT)
            dbRecord.from(data)
            dbRecord.set(Tables.WHOASSESSMENT.TUMORID, tumorId)
            dbRecord
        }

    private fun asaAssessmentsFromTumor(tumorId: Int, tumor: Tumor) =
        tumor.asaAssessments.map { data ->
            val dbRecord = context.newRecord(Tables.ASAASSESSMENT)
            dbRecord.from(data)
            dbRecord.set(Tables.ASAASSESSMENT.TUMORID, tumorId)
            dbRecord.set(Tables.ASAASSESSMENT.ASACLASSIFICATION, data.classification.name)
            dbRecord
        }

    private fun comorbidityAssessmentsFromTumor(tumorId: Int, tumor: Tumor) =
        tumor.comorbidityAssessments.map {
            extractSimpleRecord(Tables.COMORBIDITYASSESSMENT, it, "tumorId", tumorId)
        }

    private fun molecularResultsFromTumor(tumorId: Int, tumor: Tumor) =
        tumor.molecularResults.map {
            extractSimpleRecord(Tables.MOLECULARRESULT, it, "tumorId", tumorId)
        }

    private fun labMeasurementsFromTumor(tumorId: Int, tumor: Tumor) =
        tumor.labMeasurements.map { data ->
            val dbRecord = context.newRecord(Tables.LABMEASUREMENT)
            dbRecord.from(data)
            dbRecord.set(Tables.LABMEASUREMENT.TUMORID, tumorId)
            dbRecord.set(Tables.LABMEASUREMENT.NAME, data.name.name)
            dbRecord
        }

    private fun treatmentEpisodesFromTumor(tumorId: Int, tumor: Tumor) =
        tumor.treatmentEpisodes.map { data ->
            val dbRecord = context.newRecord(Tables.TREATMENTEPISODE)
            dbRecord.from(data)
            dbRecord.set(Tables.TREATMENTEPISODE.TUMORID, tumorId)
            dbRecord.set(Tables.TREATMENTEPISODE.METASTATICPRESENCE, data.metastaticPresence.name)
            dbRecord.set(Tables.TREATMENTEPISODE.REASONREFRAINMENTFROMTREATMENT, data.reasonRefrainmentFromTreatment.name)
            data to dbRecord
        }

    private fun gastroenterologyResectionsFromTreatmentEpisode(treatmentEpisodeId: Int, treatmentEpisode: TreatmentEpisode) =
        treatmentEpisode.gastroenterologyResections.map { data ->
            val dbRecord = context.newRecord(Tables.GASTROENTEROLOGYRESECTION)
            dbRecord.from(data)
            dbRecord.set(Tables.GASTROENTEROLOGYRESECTION.TREATMENTEPISODEID, treatmentEpisodeId)
            dbRecord.set(Tables.GASTROENTEROLOGYRESECTION.RESECTIONTYPE, data.resectionType.name)
            dbRecord
        }

    private fun primarySurgeriesFromTreatmentEpisode(treatmentEpisodeId: Int, treatmentEpisode: TreatmentEpisode) =
        treatmentEpisode.primarySurgeries.map { data ->
            val dbRecord = context.newRecord(Tables.PRIMARYSURGERY)
            dbRecord.from(data)
            dbRecord.set(Tables.PRIMARYSURGERY.TREATMENTEPISODEID, treatmentEpisodeId)
            dbRecord.set(Tables.PRIMARYSURGERY.TECHNIQUE, data.technique?.name)
            dbRecord.set(Tables.PRIMARYSURGERY.URGENCY, data.urgency?.name)
            dbRecord.set(Tables.PRIMARYSURGERY.RADICALITY, data.radicality?.name)
            dbRecord.set(Tables.PRIMARYSURGERY.CIRCUMFERENTIALRESECTIONMARGIN, data.circumferentialResectionMargin?.name)
            dbRecord.set(Tables.PRIMARYSURGERY.ANASTOMOTICLEAKAGEAFTERSURGERY, data.anastomoticLeakageAfterSurgery?.name)
            dbRecord
        }

    private fun metastaticSurgeriesFromTreatmentEpisode(treatmentEpisodeId: Int, treatmentEpisode: TreatmentEpisode) =
        treatmentEpisode.metastaticSurgeries.map { data ->
            val dbRecord = context.newRecord(Tables.METASTATICSURGERY)
            dbRecord.from(data)
            dbRecord.set(Tables.METASTATICSURGERY.TREATMENTEPISODEID, treatmentEpisodeId)
            dbRecord.set(Tables.METASTATICSURGERY.TYPE, data.type.name)
            dbRecord.set(Tables.METASTATICSURGERY.RADICALITY, data.radicality?.name)
            dbRecord
        }

    private fun hipecTreatmentsFromTreatmentEpisode(treatmentEpisodeId: Int, treatmentEpisode: TreatmentEpisode) =
        treatmentEpisode.hipecTreatments.map { data ->
            val dbRecord = context.newRecord(Tables.HIPECTREATMENT)
            dbRecord.from(data)
            dbRecord.set(Tables.HIPECTREATMENT.TREATMENTEPISODEID, treatmentEpisodeId)
            dbRecord
        }

    private fun primaryRadiotherapiesFromTreatmentEpisode(treatmentEpisodeId: Int, treatmentEpisode: TreatmentEpisode) =
        treatmentEpisode.primaryRadiotherapies.map { data ->
            val dbRecord = context.newRecord(Tables.PRIMARYRADIOTHERAPY)
            dbRecord.from(data)
            dbRecord.set(Tables.PRIMARYRADIOTHERAPY.TREATMENTEPISODEID, treatmentEpisodeId)
            dbRecord.set(Tables.PRIMARYRADIOTHERAPY.TYPE, data.type.name)
            dbRecord
        }

    private fun metastaticRadiotherapiesFromTreatmentEpisode(treatmentEpisodeId: Int, treatmentEpisode: TreatmentEpisode) =
        treatmentEpisode.metastaticRadiotherapies.map { data ->
            val dbRecord = context.newRecord(Tables.METASTATICRADIOTHERAPY)
            dbRecord.from(data)
            dbRecord.set(Tables.METASTATICRADIOTHERAPY.TREATMENTEPISODEID, treatmentEpisodeId)
            dbRecord.set(Tables.METASTATICRADIOTHERAPY.TYPE, data.type.name)
            dbRecord
        }

    private fun systemicTreatmentsFromTreatmentEpisode(treatmentEpisodeId: Int, treatmentEpisode: TreatmentEpisode) =
        treatmentEpisode.systemicTreatments.map { systemicTreatment ->
            val dbRecord = context.newRecord(Tables.SYSTEMICTREATMENT)
            dbRecord.from(systemicTreatment)
            dbRecord.set(Tables.SYSTEMICTREATMENT.TREATMENTEPISODEID, treatmentEpisodeId)
            systemicTreatment to dbRecord
        }

    private fun systemicTreatmentSchemesFromSystemicTreatment(systemicTreatmentId: Int, systemicTreatment: SystemicTreatment) =
        systemicTreatment.schemes.map { systemicTreatmentScheme ->
            val dbRecord = context.newRecord(Tables.SYSTEMICTREATMENTSCHEME)
            dbRecord.from(systemicTreatmentScheme)
            dbRecord.set(Tables.SYSTEMICTREATMENTSCHEME.SYSTEMICTREATMENTID, systemicTreatmentId)
            systemicTreatmentScheme to dbRecord
        }

    private fun systemicTreatmentDrugFromSystemicTreatmentScheme(schemeId: Int, scheme: SystemicTreatmentScheme) =
        scheme.components.map {
            extractSimpleRecord(Tables.SYSTEMICTREATMENTDRUG, it, "systemicTreatmentSchemeId", schemeId)
        }

    private fun responseMeasuresFromTreatmentEpisode(treatmentEpisodeId: Int, treatmentEpisode: TreatmentEpisode) =
        treatmentEpisode.responseMeasures.map { data ->
            val dbRecord = context.newRecord(Tables.RESPONSEMEASURE)
            dbRecord.from(data)
            dbRecord.set(Tables.RESPONSEMEASURE.TREATMENTEPISODEID, treatmentEpisodeId)
            dbRecord.set(Tables.RESPONSEMEASURE.RESPONSE, data.response.name)
            dbRecord
        }

    private fun progressionMeasuresFromTreatmentEpisode(treatmentEpisodeId: Int, treatmentEpisode: TreatmentEpisode) =
        treatmentEpisode.progressionMeasures.map { data ->
            val dbRecord = context.newRecord(Tables.PROGRESSIONMEASURE)
            dbRecord.from(data)
            dbRecord.set(Tables.PROGRESSIONMEASURE.TREATMENTEPISODEID, treatmentEpisodeId)
            dbRecord.set(Tables.PROGRESSIONMEASURE.TYPE, data.type.name)
            dbRecord.set(Tables.PROGRESSIONMEASURE.FOLLOWUPEVENT, data.followUpEvent?.name)
            dbRecord
        }

    private fun <T> extractSimpleRecord(table: Table<*>, item: T, foreignKeyColumnName: String, foreignKeyId: Int) =
        context.newRecord(table).let { dbRecord ->
            dbRecord.from(item)
            dbRecord.set(table.field(foreignKeyColumnName, Int::class.java), foreignKeyId)
            dbRecord as TableRecord<*>
        }

    private fun writeDrugReferences() {
        LOGGER.info { " Writing drug reference records" }
        val rows = Drug.entries.map { drug ->
            val dbRecord = context.newRecord(Tables.DRUGREFERENCE)
            dbRecord.set(Tables.DRUGREFERENCE.NAME, drug.toString())
            dbRecord.set(Tables.DRUGREFERENCE.CATEGORY, drug.category.toString())
            dbRecord
        }
        insertRows(rows, "drug")
    }

    private fun writeTumorLocationReferences() {
        LOGGER.info { " Writing tumor location reference records" }
        val rows = TumorLocation.entries.map { location ->
            val dbRecord = context.newRecord(Tables.TUMORLOCATIONREFERENCE)
            dbRecord.set(Tables.TUMORLOCATIONREFERENCE.NAME, location.toString())
            dbRecord.set(Tables.TUMORLOCATIONREFERENCE.GROUP, location.locationGroup.toString())
            dbRecord
        }
        insertRows(rows, "location")
    }

    private fun concat(items: Iterable<Any>): String {
        return items.joinToString(";")
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}

        fun fromCredentials(user: String, pass: String, url: String): DatabaseWriter {
            // Disable annoying jooq self-ad messages
            System.setProperty("org.jooq.no-logo", "true")
            System.setProperty("org.jooq.no-tips", "true")
            val conn = DriverManager.getConnection("jdbc:$url", user, pass)

            LOGGER.info { "Connecting to database '${conn.catalog}'" }
            val context = DSL.using(conn, SQLDialect.MYSQL)
            return DatabaseWriter(context, conn)
        }
    }
}