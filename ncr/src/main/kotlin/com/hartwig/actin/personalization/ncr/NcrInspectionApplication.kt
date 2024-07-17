package com.hartwig.actin.personalization.ncr

import com.hartwig.actin.personalization.datamodel.PatientRecord
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.PatientRecordFactory
import com.hartwig.actin.personalization.ncr.serialization.NcrDataReader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import picocli.CommandLine
import java.util.concurrent.Callable

class NCRInspectionApplication : Callable<Int> {

    @CommandLine.Option(names = ["-ncr_dataset_csv"], required = true, description = ["File containing the NCR dataset"])
    lateinit var ncrDatasetPath: String

    override fun call(): Int {
        LOGGER.info("Running NCR inspection application")

        LOGGER.info("Reading NCR dataset from {}", ncrDatasetPath)
        val ncrRecords = NcrDataReader.read(ncrDatasetPath)
        LOGGER.info(" Read {} NCR records", ncrRecords.size)

        LOGGER.info("Printing overview of NCR records")

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

        LOGGER.info("Creating patient records from NCR records")
        val patientRecords: List<PatientRecord> = PatientRecordFactory.default().create(ncrRecords)
        LOGGER.info(" Created {} patient records from {} NCR records", patientRecords.count(), ncrRecords.count())

        LOGGER.info("Done!")
        return 0
    }

    private fun printIdentificationOverview(ncrRecords: List<NcrRecord>) {
        val patientCount: Int = ncrRecords.map { it.identification.keyNkr }.distinct().count()
        val tumorCount: Int = ncrRecords.map { it.identification.keyZid }.distinct().count()
        val episodeCount: Int = ncrRecords.map { it.identification.keyEid }.distinct().count()

        LOGGER.info(" {} unique patients found, with {} tumors and {} episodes", patientCount, tumorCount, episodeCount)
    }

    private fun printPatientCharacteristicsOverview(ncrRecords: List<NcrRecord>) {
        LOGGER.info(" Printing (some) patient characteristics")
        val recordCountPerVitalStatus: Map<Int?, Int> =
            countNullableRecords(ncrRecords.map { it.identification.keyEid to it.patientCharacteristics.vitStat })
        recordCountPerVitalStatus.entries.forEach { LOGGER.info("  {} records have vital status '{}'", it.value, it.key) }

        val recordCountPerSex: Map<Int, Int> = countRecords(ncrRecords.map { it.identification.keyEid to it.patientCharacteristics.gesl })
        recordCountPerSex.entries.forEach { LOGGER.info("  {} records have sex '{}'", it.value, it.key) }

        val recordCountPerPerfStat: Map<Int?, Int> =
            countNullableRecords(ncrRecords.map { it.identification.keyEid to it.patientCharacteristics.perfStat })
        recordCountPerPerfStat.entries.forEach { LOGGER.info("  {} records have WHO score '{}'", it.value, it.key) }
    }

    private fun printClinicalCharacteristicsOverview(ncrRecords: List<NcrRecord>) {
        LOGGER.info(" Printing (some) clinical characteristics")
        val recordCountPerDoubleTumor: Map<Int?, Int> =
            countNullableRecords(ncrRecords.map { it.identification.keyEid to it.clinicalCharacteristics.dubbeltum })
        recordCountPerDoubleTumor.entries.forEach { LOGGER.info("  {} records have double tumor '{}'", it.value, it.key) }

        val recordCountPerIleus: Map<Int?, Int> =
            countNullableRecords(ncrRecords.map { it.identification.keyEid to it.clinicalCharacteristics.ileus })
        recordCountPerIleus.entries.forEach { LOGGER.info("  {} records have ileus '{}'", it.value, it.key) }
    }

    private fun printMolecularCharacteristicsOverview(ncrRecords: List<NcrRecord>) {
        LOGGER.info(" Printing (some) molecular characteristics")
        val recordCountPerMSIStatus: Map<Int?, Int> =
            countNullableRecords(ncrRecords.map { it.identification.keyEid to it.molecularCharacteristics.msiStat })
        recordCountPerMSIStatus.entries.forEach { LOGGER.info("  {} records have MSI status '{}'", it.value, it.key) }

        val recordCountPerBRAFStatus: Map<Int?, Int> =
            countNullableRecords(ncrRecords.map { it.identification.keyEid to it.molecularCharacteristics.brafMut })
        recordCountPerBRAFStatus.entries.forEach { LOGGER.info("  {} records have BRAF status '{}'", it.value, it.key) }

        val recordCountPerRASStatus: Map<Int?, Int> =
            countNullableRecords(ncrRecords.map { it.identification.keyEid to it.molecularCharacteristics.rasMut })
        recordCountPerRASStatus.entries.forEach { LOGGER.info("  {} records have RAS status '{}'", it.value, it.key) }
    }

    private fun printPriorMalignancyOverview(ncrRecords: List<NcrRecord>) {
        LOGGER.info(" Printing (some) prior malignancy characteristics")
        val recordsWithNoPriorMalignancies = ncrRecords.filter {
            it.priorMalignancies.mal1Int == null && it.priorMalignancies.mal2Int == null
                    && it.priorMalignancies.mal3Int == null && it.priorMalignancies.mal4Int == null
        }
        val recordsWithOnePriorMalignancy =
            ncrRecords.filter {
                it.priorMalignancies.mal1Int != null && it.priorMalignancies.mal2Int == null &&
                        it.priorMalignancies.mal3Int == null && it.priorMalignancies.mal4Int == null
            }
        val recordsWithTwoPriorMalignancies = ncrRecords.filter {
            it.priorMalignancies.mal1Int != null && it.priorMalignancies.mal2Int != null &&
                    it.priorMalignancies.mal3Int == null && it.priorMalignancies.mal4Int == null
        }
        val recordsWithThreePriorMalignancies = ncrRecords.filter {
            it.priorMalignancies.mal1Int != null && it.priorMalignancies.mal2Int != null &&
                    it.priorMalignancies.mal3Int != null && it.priorMalignancies.mal4Int == null
        }
        val recordsWithFourPriorMalignancies = ncrRecords.filter {
            it.priorMalignancies.mal1Int != null && it.priorMalignancies.mal2Int != null &&
                    it.priorMalignancies.mal3Int != null && it.priorMalignancies.mal4Int != null
        }

        LOGGER.info("  {} records have no prior malignancies", recordsWithNoPriorMalignancies.count())
        LOGGER.info("  {} records have 1 prior malignancy", recordsWithOnePriorMalignancy.count())
        LOGGER.info("  {} records have 2 prior malignancies", recordsWithTwoPriorMalignancies.count())
        LOGGER.info("  {} records have 3 prior malignancies", recordsWithThreePriorMalignancies.count())
        LOGGER.info("  {} records have 4 prior malignancies", recordsWithFourPriorMalignancies.count())
    }

    private fun printPrimaryDiagnosisOverview(ncrRecords: List<NcrRecord>) {
        LOGGER.info(" Printing (some) primary diagnosis data")

        val recordsPerYearOfIncidence: Map<Int, Int> =
            countRecords(ncrRecords.map { it.identification.keyEid to it.primaryDiagnosis.incjr })
        recordsPerYearOfIncidence.entries.forEach { LOGGER.info("  {} records have incidence year '{}'", it.value, it.key) }

        val recordsPerTumorType: Map<String, Int> =
            countRecords(ncrRecords.map { it.identification.keyEid to it.primaryDiagnosis.topoSublok })
        recordsPerTumorType.entries.forEach { LOGGER.info("  {} records have tumor type '{}'", it.value, it.key) }

        val recordsPerStadium: Map<String?, Int> =
            countNullableRecords(ncrRecords.map { it.identification.keyEid to it.primaryDiagnosis.stadium })
        recordsPerStadium.entries.forEach { LOGGER.info("  {} records have stadium '{}'", it.value, it.key) }
    }

    private fun printMetastaticDiagnosisOverview(ncrRecords: List<NcrRecord>) {
        LOGGER.info(" Printing (some) metastatic diagnosis data")
        val recordsWithNoMetastases = ncrRecords.filter {
            it.metastaticDiagnosis.metaInt1 == null && it.metastaticDiagnosis.metaInt2 == null
                    && it.metastaticDiagnosis.metaInt3 == null && it.metastaticDiagnosis.metaInt4 == null
        }
        val recordsWithOneMetastasis = ncrRecords.filter {
            it.metastaticDiagnosis.metaInt1 != null && it.metastaticDiagnosis.metaInt2 == null
                    && it.metastaticDiagnosis.metaInt3 == null && it.metastaticDiagnosis.metaInt4 == null
        }
        val recordsWithTwoMetastases = ncrRecords.filter {
            it.metastaticDiagnosis.metaInt1 != null && it.metastaticDiagnosis.metaInt2 != null
                    && it.metastaticDiagnosis.metaInt3 == null && it.metastaticDiagnosis.metaInt4 == null
        }
        val recordsWithThreeMetastases = ncrRecords.filter {
            it.metastaticDiagnosis.metaInt1 != null && it.metastaticDiagnosis.metaInt2 != null
                    && it.metastaticDiagnosis.metaInt3 != null && it.metastaticDiagnosis.metaInt4 == null
        }
        val recordsWithAtLeastFourMetastases = ncrRecords.filter {
            it.metastaticDiagnosis.metaInt1 != null && it.metastaticDiagnosis.metaInt2 != null
                    && it.metastaticDiagnosis.metaInt3 != null && it.metastaticDiagnosis.metaInt4 != null
        }

        LOGGER.info("  {} records have no metastases", recordsWithNoMetastases.count())
        LOGGER.info("  {} records have 1 metastasis", recordsWithOneMetastasis.count())
        LOGGER.info("  {} records have 2 metastases", recordsWithTwoMetastases.count())
        LOGGER.info("  {} records have 3 metastases", recordsWithThreeMetastases.count())
        LOGGER.info("  {} records have (at least) 4 metastases", recordsWithAtLeastFourMetastases.count())
    }

    private fun printComorbidityOverview(ncrRecords: List<NcrRecord>) {
        LOGGER.info(" Printing (some) comorbidity data")

        val recordsPerComorbidity: Map<Int?, Int> =
            countNullableRecords(ncrRecords.map { it.identification.keyEid to it.comorbidities.cci })
        recordsPerComorbidity.entries.forEach { LOGGER.info("  {} records have Charlson comorbidity index '{}'", it.value, it.key) }
    }

    private fun printLabValueOverview(ncrRecords: List<NcrRecord>) {
        LOGGER.info(" Printing (some) lab data")

        val recordsWithAtLeastOneLDHMeasure = ncrRecords.filter { it.labValues.ldh1 != null }
        val recordsWithAtLeastOneAFMeasure = ncrRecords.filter { it.labValues.af1 != null }
        val recordsWithAtLeastOneNeutroMeasure = ncrRecords.filter { it.labValues.neutro1 != null }
        val recordsWithAtLeastOneAlbumineMeasure = ncrRecords.filter { it.labValues.albumine1 != null }
        val recordsWithAtLeastOneLeukoMeasure = ncrRecords.filter { it.labValues.leuko1 != null }

        LOGGER.info("  {} records have at least 1 LDH measure", recordsWithAtLeastOneLDHMeasure.count())
        LOGGER.info("  {} records have at least 1 AF measure", recordsWithAtLeastOneAFMeasure.count())
        LOGGER.info("  {} records have at least 1 Neutro measure", recordsWithAtLeastOneNeutroMeasure.count())
        LOGGER.info("  {} records have at least 1 Albumine measure", recordsWithAtLeastOneAlbumineMeasure.count())
        LOGGER.info("  {} records have at least 1 Leuko measure", recordsWithAtLeastOneLeukoMeasure.count())
    }

    private fun printTreatmentOverview(ncrRecords: List<NcrRecord>) {
        LOGGER.info(" Printing (some) treatment data")

        val recordsPerTrialParticipation: Map<Int?, Int> =
            countNullableRecords(ncrRecords.map { it.identification.keyEid to it.treatment.deelnameStudie })
        recordsPerTrialParticipation.entries.forEach { LOGGER.info("  {} records have trial participation '{}'", it.value, it.key) }

        val recordsPerTreatmentGiven: Map<Int?, Int> =
            countNullableRecords(ncrRecords.map { it.identification.keyEid to it.treatment.tumgerichtTher })
        recordsPerTreatmentGiven.entries.forEach { LOGGER.info("  {} records have treatment given '{}'", it.value, it.key) }

        val recordsWithGastroenterologyResection = ncrRecords.filter { it.treatment.gastroenterologyResection.mdlRes != null }
        LOGGER.info("  {} records have had a gastroenterology resection", recordsWithGastroenterologyResection.count())

        val recordsWithPrimarySurgery = ncrRecords.filter { it.treatment.primarySurgery.chir != null }
        LOGGER.info("  {} records have had a primary surgery", recordsWithPrimarySurgery.count())

        val recordsWithAtLeastOneMetastaticSurgery = ncrRecords.filter { it.treatment.metastaticSurgery.metaChirCode1 != null }
        LOGGER.info("  {} records have had at least one metastatic surgery", recordsWithAtLeastOneMetastaticSurgery.count())

        val recordsPerPrimaryRadiotherapy: Map<Int, Int> =
            countRecords(ncrRecords.map { it.identification.keyEid to it.treatment.primaryRadiotherapy.rt })
        recordsPerPrimaryRadiotherapy.entries.forEach { LOGGER.info("  {} records have primary radiotherapy '{}'", it.value, it.key) }

        val recordsWithAtLeastOneMetastaticRadiotherapy = ncrRecords.filter { it.treatment.metastaticRadiotherapy.metaRtCode1 != null }
        LOGGER.info("  {} records have had at least one metastatic radiotherapy", recordsWithAtLeastOneMetastaticRadiotherapy.count())

        val recordsPerChemotherapy: Map<Int, Int> =
            countRecords(ncrRecords.map { it.identification.keyEid to it.treatment.systemicTreatment.chemo })
        recordsPerChemotherapy.entries.forEach { LOGGER.info("  {} records have chemotherapy '{}'", it.value, it.key) }

        val recordsPerTargetedTherapy: Map<Int, Int> =
            countRecords(ncrRecords.map { it.identification.keyEid to it.treatment.systemicTreatment.target })
        recordsPerTargetedTherapy.entries.forEach { LOGGER.info("  {} records have targeted therapy '{}'", it.value, it.key) }

        val recordsWithHIPEC = ncrRecords.filter { it.treatment.hipec.hipec != null }
        LOGGER.info("  {} records have had HIPEC", recordsWithHIPEC.count())
    }

    private fun printTreatmentResponseOverview(ncrRecords: List<NcrRecord>) {
        LOGGER.info(" Printing (some) treatment response data")

        val recordsPerResponse: Map<String?, Int> =
            countNullableRecords(ncrRecords.map { it.identification.keyEid to it.treatmentResponse.responsUitslag })
        recordsPerResponse.entries.forEach { LOGGER.info("  {} records have treatment response '{}'", it.value, it.key) }

        val recordsWithAtLeastOnePFS = ncrRecords.filter { it.treatmentResponse.pfsEvent1 != null }
        LOGGER.info("  {} records have at least one PFS", recordsWithAtLeastOnePFS.count())
    }

    private fun <T : Comparable<T>> countRecords(listPerPatient: List<Pair<Int, T>>): Map<T, Int> {
        return listPerPatient.groupingBy { it.second }.eachCount().toSortedMap()
    }

    private fun <T : Comparable<T>> countNullableRecords(listPerPatient: List<Pair<Int, T?>>): Map<T?, Int> {
        return listPerPatient.groupingBy { it.second }.eachCount().toSortedMap(nullsFirst())
    }

    companion object {
        private val LOGGER: Logger = LogManager.getLogger(NCRInspectionApplication::class)
    }
}

fun main(args: Array<String>): Unit = kotlin.system.exitProcess(CommandLine(NCRInspectionApplication()).execute(*args))
