package com.hartwig.actin.personalization.ncr

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.ReferencePatientFactory
import com.hartwig.actin.personalization.ncr.serialization.NcrDataReader
import io.github.oshai.kotlinlogging.KotlinLogging
import picocli.CommandLine
import java.util.concurrent.Callable

class NCRInspectionApplication : Callable<Int> {

    @CommandLine.Option(names = ["-ncr_file"], required = true)
    lateinit var ncrFile: String

    override fun call(): Int {
        LOGGER.info { "Running NCR inspection application" }

        LOGGER.info { "Reading NCR dataset from $ncrFile" }
        val ncrRecords = NcrDataReader.read(ncrFile)
        LOGGER.info { " Read ${ncrRecords.size} NCR records" }

        LOGGER.info { "Printing overview of NCR records" }

        printIdentificationOverview(ncrRecords)
        printPatientCharacteristicsOverview(ncrRecords)
        printClinicalCharacteristicsOverview(ncrRecords)
        printMolecularCharacteristicsOverview(ncrRecords)
        printPriorMalignancyOverview(ncrRecords)
        printPrimaryDiagnosisOverview(ncrRecords)
        printMetastaticDiagnosisOverview(ncrRecords)
        printComorbidityOverview(ncrRecords)
        printLabValueOverview(ncrRecords)
        printTreatmentOverview(ncrRecords)
        printTreatmentResponseOverview(ncrRecords)

        LOGGER.info { "Creating patient records from NCR records" }
        val patientRecords = ReferencePatientFactory.create(ncrRecords)
        LOGGER.info { " Created ${patientRecords.size} patient records from ${ncrRecords.size} NCR records" }

        LOGGER.info { "Done!" }
        return 0
    }

    private fun printIdentificationOverview(ncrRecords: List<NcrRecord>) {
        val patientCount = ncrRecords.map { it.identification.keyNkr }.distinct().count()
        val tumorCount = ncrRecords.map { it.identification.keyZid }.distinct().count()
        val episodeCount = ncrRecords.map { it.identification.keyEid }.distinct().count()

        LOGGER.info { " $patientCount unique patients found, with $tumorCount tumors and $episodeCount episodes" }
    }

    private fun <T : Comparable<T>> logMapSummaryForField(ncrRecords: List<NcrRecord>, description: String, getField: (NcrRecord) -> T?) {
        countNullableRecords(ncrRecords.map { it.identification.keyEid to getField.invoke(it) })
            .entries.forEach { (key, count) -> LOGGER.info { "  $count records have $description '$key'" } }
    }

    private fun printPatientCharacteristicsOverview(ncrRecords: List<NcrRecord>) {
        LOGGER.info { " Printing (some) patient characteristics" }
        logMapSummaryForField(ncrRecords, "vital status") { it.patientCharacteristics.vitStat }
        logMapSummaryForField(ncrRecords, "sex") { it.patientCharacteristics.gesl }
        logMapSummaryForField(ncrRecords, "WHO score") { it.patientCharacteristics.perfStat }
    }

    private fun printClinicalCharacteristicsOverview(ncrRecords: List<NcrRecord>) {
        LOGGER.info { " Printing (some) clinical characteristics" }
        logMapSummaryForField(ncrRecords, "double tumor") { it.clinicalCharacteristics.dubbeltum }
        logMapSummaryForField(ncrRecords, "ileus") { it.clinicalCharacteristics.ileus }
    }

    private fun printMolecularCharacteristicsOverview(ncrRecords: List<NcrRecord>) {
        LOGGER.info { " Printing (some) molecular characteristics" }
        logMapSummaryForField(ncrRecords, "MSI status") { it.molecularCharacteristics.msiStat }
        logMapSummaryForField(ncrRecords, "BRAF status") { it.molecularCharacteristics.brafMut }
        logMapSummaryForField(ncrRecords, "RAS status") { it.molecularCharacteristics.rasMut }
    }

    private fun printPriorMalignancyOverview(ncrRecords: List<NcrRecord>) {
        LOGGER.info { " Printing (some) prior malignancy characteristics" }
        ncrRecords.map { ncrRecord ->
            with(ncrRecord.priorMalignancies) { listOfNotNull(mal1Int, mal2Int, mal3Int, mal4Int).size }
        }
            .groupingBy { it }.eachCount().toSortedMap()
            .forEach { (numPriors, count) -> LOGGER.info { "  $count records have $numPriors prior malignancies" } }
    }

    private fun printPrimaryDiagnosisOverview(ncrRecords: List<NcrRecord>) {
        LOGGER.info { " Printing (some) primary diagnosis data" }
        logMapSummaryForField(ncrRecords, "incidence year") { it.primaryDiagnosis.incjr }
        logMapSummaryForField(ncrRecords, "tumor type") { it.primaryDiagnosis.topoSublok }
        logMapSummaryForField(ncrRecords, "stadium") { it.primaryDiagnosis.stadium }
    }

    private fun printMetastaticDiagnosisOverview(ncrRecords: List<NcrRecord>) {
        LOGGER.info { " Printing (some) metastatic diagnosis data" }
        ncrRecords.map { ncrRecord ->
            with(ncrRecord.metastaticDiagnosis) {
                listOfNotNull(metaInt1, metaInt2, metaInt3, metaInt4, metaInt5, metaInt6, metaInt7, metaInt8, metaInt9, metaInt10).size
            }
        }
            .groupingBy { it }.eachCount().toSortedMap()
            .forEach { (numMetastases, count) -> LOGGER.info { "  $count records have $numMetastases metastases" } }
    }

    private fun printComorbidityOverview(ncrRecords: List<NcrRecord>) {
        LOGGER.info { " Printing (some) comorbidity data" }
        logMapSummaryForField(ncrRecords, "Charlson comorbidity index") { it.comorbidities.cci }
    }

    private fun printLabValueOverview(ncrRecords: List<NcrRecord>) {
        LOGGER.info { " Printing (some) lab data" }

        LOGGER.info { "  ${ncrRecords.count { it.labValues.ldh1 != null }} records have at least 1 LDH measure" }
        LOGGER.info { "  ${ncrRecords.count { it.labValues.af1 != null }} records have at least 1 AF measure" }
        LOGGER.info { "  ${ncrRecords.count { it.labValues.neutro1 != null }} records have at least 1 Neutro measure" }
        LOGGER.info { "  ${ncrRecords.count { it.labValues.albumine1 != null }} records have at least 1 Albumine measure" }
        LOGGER.info { "  ${ncrRecords.count { it.labValues.leuko1 != null }} records have at least 1 Leuko measure" }
    }

    private fun printTreatmentOverview(ncrRecords: List<NcrRecord>) {
        LOGGER.info { " Printing (some) treatment data" }

        logMapSummaryForField(ncrRecords, "trial participation") { it.treatment.deelnameStudie }
        logMapSummaryForField(ncrRecords, "treatment given") { it.treatment.tumgerichtTher }

        LOGGER.info {
            "  ${ncrRecords.count { it.treatment.gastroenterologyResection.mdlRes != null }} records have had a gastroenterology resection"
        }
        LOGGER.info { "  ${ncrRecords.count { it.treatment.primarySurgery.chir != null }} records have had a primary surgery" }
        val numRecordsWithMetastaticSurgery = ncrRecords.count { it.treatment.metastaticSurgery.metaChirCode1 != null }
        LOGGER.info { "  $numRecordsWithMetastaticSurgery records have had at least one metastatic surgery" }

        logMapSummaryForField(ncrRecords, "primary radiotherapy") { it.treatment.primaryRadiotherapy.rt }

        val recordsWithAtLeastOneMetastaticRadiotherapy = ncrRecords.count { it.treatment.metastaticRadiotherapy.metaRtCode1 != null }
        LOGGER.info { "  $recordsWithAtLeastOneMetastaticRadiotherapy records have had at least one metastatic radiotherapy" }

        logMapSummaryForField(ncrRecords, "chemotherapy") { it.treatment.systemicTreatment.chemo }
        logMapSummaryForField(ncrRecords, "targeted therapy") { it.treatment.systemicTreatment.target }

        LOGGER.info { "  ${ncrRecords.count { it.treatment.hipec.hipec != null }} records have had HIPEC" }
    }

    private fun printTreatmentResponseOverview(ncrRecords: List<NcrRecord>) {
        LOGGER.info { " Printing (some) treatment response data" }

        logMapSummaryForField(ncrRecords, "treatment response") { it.treatmentResponse.responsUitslag }
        LOGGER.info { "  ${ncrRecords.count { it.treatmentResponse.pfsEvent1 != null }} records have at least one PFS" }
    }

    private fun <T : Comparable<T>> countNullableRecords(listPerPatient: List<Pair<Int, T?>>): Map<T?, Int> {
        return listPerPatient.groupingBy { it.second }.eachCount().toSortedMap(nullsFirst())
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}

fun main(args: Array<String>): Unit = kotlin.system.exitProcess(CommandLine(NCRInspectionApplication()).execute(*args))
