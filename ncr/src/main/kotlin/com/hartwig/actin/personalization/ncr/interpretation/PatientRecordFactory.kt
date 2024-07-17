package com.hartwig.actin.personalization.ncr.interpretation

import com.hartwig.actin.personalization.datamodel.PatientRecord
import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.datamodel.TumorEntry
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.extractor.NcrEpisodeExtractor
import com.hartwig.actin.personalization.ncr.interpretation.extractor.NcrSystemicTreatmentPlanExtractor
import com.hartwig.actin.personalization.ncr.interpretation.extractor.NcrTumorEntryExtractor
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrSexMapper
import java.util.stream.Collectors

const val DIAGNOSIS_EPISODE = "DIA"

class PatientRecordFactory(private val tumorEntryExtractor: NcrTumorEntryExtractor) {

    fun create(ncrRecords: List<NcrRecord>): List<PatientRecord> {
        return ncrRecords.groupBy { it.identification.keyNkr }.values.parallelStream()
            .map(::createPatientRecord)
            .collect(Collectors.toList())
    }

    private fun createPatientRecord(ncrRecords: List<NcrRecord>): PatientRecord {
        return PatientRecord(
            ncrId = extractNcrId(ncrRecords),
            sex = extractSex(ncrRecords),
            isAlive = determineIsAlive(ncrRecords),
            tumorEntries = determineEpisodesPerTumorOfInterest(ncrRecords)
        )
    }

    private fun extractNcrId(ncrRecords: List<NcrRecord>): Int {
        return ncrRecords.map { it.identification.keyNkr }.distinct().single()
    }

    private fun extractSex(ncrRecords: List<NcrRecord>): Sex {
        return NcrSexMapper.resolve(ncrRecords.map { it.patientCharacteristics.gesl }.distinct().single())
    }

    private fun determineIsAlive(ncrRecords: List<NcrRecord>): Boolean {
        // Vital status is only collect on diagnosis episodes.
        val vitalStatus = diagnosisEpisodes(ncrRecords).map { it.patientCharacteristics.vitStat }.distinct().single()
       
        return when (vitalStatus) {
            0 -> true
            1 -> false
            else -> throw IllegalStateException("Cannot convert vital status: $vitalStatus")
        }
    }

    private fun determineEpisodesPerTumorOfInterest(ncrRecords: List<NcrRecord>): List<TumorEntry> {
        return ncrRecords.groupBy { it.identification.keyZid }.entries
            .map { (_, records) -> tumorEntryExtractor.extractTumorEntry(records) }
    }

    private fun diagnosisEpisodes(ncrRecords: List<NcrRecord>): List<NcrRecord> {
        return ncrRecords.filter { it.identification.epis == DIAGNOSIS_EPISODE }
    }

    companion object {
        fun default(): PatientRecordFactory {
            return PatientRecordFactory(NcrTumorEntryExtractor(NcrEpisodeExtractor(NcrSystemicTreatmentPlanExtractor())))
        }
    }
}
