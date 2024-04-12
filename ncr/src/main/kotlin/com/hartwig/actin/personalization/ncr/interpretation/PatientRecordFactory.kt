package com.hartwig.actin.personalization.ncr.interpretation

import com.hartwig.actin.personalization.ncr.datamodel.DiagnosisEpisode
import com.hartwig.actin.personalization.ncr.datamodel.PatientRecord
import com.hartwig.actin.personalization.ncr.datamodel.Sex
import com.hartwig.actin.personalization.ncr.datamodel.TNM_M
import com.hartwig.actin.personalization.ncr.datamodel.TNM_N
import com.hartwig.actin.personalization.ncr.datamodel.TNM_T
import com.hartwig.actin.personalization.ncr.datamodel.TumorEpisodes
import com.hartwig.actin.personalization.ncr.datamodel.TumorLocation
import com.hartwig.actin.personalization.ncr.datamodel.TumorOfInterest
import com.hartwig.actin.personalization.ncr.datamodel.TumorSubLocation
import com.hartwig.actin.personalization.ncr.datamodel.TumorType
import com.hartwig.actin.personalization.ncr.serialization.datamodel.NCRRecord
import java.util.stream.Collectors

private const val DIAGNOSIS_EPISODE = "DIA"

class PatientRecordFactory {

    fun create(ncrRecords: List<NCRRecord>): List<PatientRecord> {
        val recordsPerPatient: Map<Int, List<NCRRecord>> = ncrRecords.groupBy { it.identification.keyNkr }

        return recordsPerPatient.entries.parallelStream()
            .map { createPatientRecord(it.value) }
            .collect(Collectors.toList())
    }

    private fun createPatientRecord(ncrRecords: List<NCRRecord>): PatientRecord {
        return PatientRecord(
            ncrId = extractNcrId(ncrRecords),
            sex = extractSex(ncrRecords),
            isAlive = determineIsAlive(ncrRecords),
            episodesPerTumorOfInterest = determineEpisodesPerTumorOfInterest(ncrRecords),
            priorTumors = listOf()
        )
    }

    private fun extractNcrId(ncrRecords: List<NCRRecord>): Int {
        val ncrIds: List<Int> = ncrRecords.map { it.identification.keyNkr }.distinct()
        if (ncrIds.count() != 1) {
            throw IllegalStateException("Non-unique or missing NCR ID when creating a single patient record: $ncrIds")
        }
        return ncrIds[0]
    }

    private fun extractSex(ncrRecords: List<NCRRecord>): Sex {
        val sexes: List<Int> = ncrRecords.map { it.patientCharacteristics.gesl }.distinct()
        if (sexes.count() != 1) {
            throw IllegalStateException("Multiple sexes found for patient with NCR ID '" + extractNcrId(ncrRecords) + "'")
        }

        return NcrCodeResolver.resolve(sexes.single())
    }

    private fun determineIsAlive(ncrRecords: List<NCRRecord>): Boolean? {
        // Vital status is only collect on diagnosis episodes.
        val vitalStatuses: List<Int?> = diagnosisEpisodes(ncrRecords).map { it.patientCharacteristics.vitStat }.distinct()
        if (vitalStatuses.count() != 1) {
            throw IllegalStateException("Non-unique or missing vital statuses when creating a single patient record: $vitalStatuses")
        }
        return when (val vitalStatus = vitalStatuses[0]) {
            null -> return null
            0 -> true
            1 -> false
            else -> throw IllegalStateException("Cannot convert vital status: $vitalStatus")
        }
    }

    private fun determineEpisodesPerTumorOfInterest(ncrRecords: List<NCRRecord>): Map<TumorOfInterest, TumorEpisodes> {
        val recordsPerTumor = ncrRecords.groupBy { it.identification.keyZid }
        return recordsPerTumor.values.associate(::createEpisodesForOneTumorOfInterest)
    }

    private fun createEpisodesForOneTumorOfInterest(ncrRecords: List<NCRRecord>): Pair<TumorOfInterest, TumorEpisodes> {
        return Pair(createTumorOfInterest(ncrRecords), createTumorEpisodes(ncrRecords))
    }

    private fun createTumorOfInterest(ncrRecords: List<NCRRecord>): TumorOfInterest {
        return TumorOfInterest(
            consolidatedTumorType = TumorType.ADENOCARCINOMA_DIFFUSE_TYPE,
            consolidatedTumorSubLocation = TumorSubLocation.UNKNOWN_PRIMARY_TUMOR,
            consolidatedTumorLocation = TumorLocation.ADRENAL,
            hasHadTumorDirectedSystemicTherapy = false,
            hasHadPriorTumor = false,
            intervalsTumorIncidenceDiagnosisTumorPrior = listOf()
        )
    }

    private fun createTumorEpisodes(ncrRecords: List<NCRRecord>): TumorEpisodes {
        val (ncrDiagnosisEpisodes, ncrTreatmentEpisodes) = ncrRecords.partition { it.identification.epis == DIAGNOSIS_EPISODE }
        val diagnosisEpisode = ncrDiagnosisEpisodes.minBy { it.identification.keyEid }.let {
            DiagnosisEpisode(
                id = it.identification.keyEid,
                order = it.identification.teller,
                whoStatusPreTreatmentStart = it.patientCharacteristics.perfStat,
                asaClassificationPreSurgeryOrEndoscopy = it.patientCharacteristics.asa?.let(NcrCodeResolver::resolve),
                tumorIncidenceYear = it.primaryDiagnosis.incjr,
                tumorBasisOfDiagnosis = NcrCodeResolver.resolve(it.primaryDiagnosis.diagBasis),
                tumorLocation = TumorLocation.OTHER_AND_ILL_DEFINED_LOCALIZATIONS, // TODO
                tumorDifferentiationGrade = NcrCodeResolver.resolve(it.primaryDiagnosis.diffgrad.toInt()),
                tnmCT = enumValueOf<TNM_T>(it.primaryDiagnosis.ct),
                tnmCN = enumValueOf<TNM_N>(it.primaryDiagnosis.cn),
                tnmCM = enumValueOf<TNM_M>(it.primaryDiagnosis.cm),
                tnmPT = it.primaryDiagnosis.pt?.let(::enumValueOf),
                tnmPN = it.primaryDiagnosis.pn?.let(::enumValueOf),
                tnmPM = it.primaryDiagnosis.pm?.let(::enumValueOf),
                stageCTNM = it.primaryDiagnosis.cstadium?.let(::enumValueOf),
                stagePTNM = it.primaryDiagnosis.pstadium?.let(::enumValueOf),
                stageTNM = it.primaryDiagnosis.stadium?.let(::enumValueOf),
                numberOfInvestigatedLymphNodes = it.primaryDiagnosis.ondLymf,
                numberOfPositiveLymphNodes = it.primaryDiagnosis.posLymf,
                distantMetastasesStatus = NcrCodeResolver.resolve(it.identification.metaEpis),
            )
        }
    }

    private fun diagnosisEpisodes(ncrRecords: List<NCRRecord>): List<NCRRecord> {
        return ncrRecords.filter { it.identification.epis == DIAGNOSIS_EPISODE }
    }
}