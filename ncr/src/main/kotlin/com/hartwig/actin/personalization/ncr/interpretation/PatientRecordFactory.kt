package com.hartwig.actin.personalization.ncr.interpretation

import com.hartwig.actin.personalization.datamodel.Diagnosis
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.PatientRecord
import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.extractor.extractDiagnosisAndEpisodes
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrSexMapper
import java.util.stream.Collectors

const val DIAGNOSIS_EPISODE = "DIA"

object PatientRecordFactory {

    fun create(ncrRecords: List<NcrRecord>): List<PatientRecord> {
        val recordsPerPatient: Map<Int, List<NcrRecord>> = ncrRecords.groupBy { it.identification.keyNkr }

        return recordsPerPatient.values.parallelStream()
            .map(::createPatientRecord)
            .collect(Collectors.toList())
    }

    private fun createPatientRecord(ncrRecords: List<NcrRecord>): PatientRecord {
        return PatientRecord(
            ncrId = extractNcrId(ncrRecords),
            sex = extractSex(ncrRecords),
            isAlive = determineIsAlive(ncrRecords),
            episodesPerTumorOfInterest = determineEpisodesPerTumorOfInterest(ncrRecords)
        )
    }

    private fun extractNcrId(ncrRecords: List<NcrRecord>): Int {
        val ncrIds: List<Int> = ncrRecords.map { it.identification.keyNkr }.distinct()
        if (ncrIds.count() != 1) {
            throw IllegalStateException("Non-unique or missing NCR ID when creating a single patient record: $ncrIds")
        }
        return ncrIds[0]
    }

    private fun extractSex(ncrRecords: List<NcrRecord>): Sex {
        val sexes: List<Int> = ncrRecords.map { it.patientCharacteristics.gesl }.distinct()
        if (sexes.count() != 1) {
            throw IllegalStateException("Multiple sexes found for patient with NCR ID '" + extractNcrId(ncrRecords) + "'")
        }

        return NcrSexMapper.resolve(sexes.single())
    }

    private fun determineIsAlive(ncrRecords: List<NcrRecord>): Boolean? {
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

    private fun determineEpisodesPerTumorOfInterest(ncrRecords: List<NcrRecord>): Map<Diagnosis, List<Episode>> {
        return ncrRecords.groupBy { it.identification.keyZid }.entries
            .associate { (tumorId, records) -> extractDiagnosisAndEpisodes(tumorId, records) }
    }

    private fun diagnosisEpisodes(ncrRecords: List<NcrRecord>): List<NcrRecord> {
        return ncrRecords.filter { it.identification.epis == DIAGNOSIS_EPISODE }
    }
}
