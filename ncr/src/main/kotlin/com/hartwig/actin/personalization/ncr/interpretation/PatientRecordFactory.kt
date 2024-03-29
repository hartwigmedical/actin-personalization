package com.hartwig.actin.personalization.ncr.interpretation

import com.hartwig.actin.personalization.ncr.datamodel.PatientRecord
import com.hartwig.actin.personalization.ncr.datamodel.Sex
import com.hartwig.actin.personalization.ncr.datamodel.TumorEpisodes
import com.hartwig.actin.personalization.ncr.datamodel.TumorLocation
import com.hartwig.actin.personalization.ncr.datamodel.TumorOfInterest
import com.hartwig.actin.personalization.ncr.datamodel.TumorSubLocation
import com.hartwig.actin.personalization.ncr.datamodel.TumorType
import com.hartwig.actin.personalization.ncr.serialization.datamodel.NCRRecord

object PatientRecordFactory {

    fun create(ncrRecords: List<NCRRecord>): List<PatientRecord> {
        val recordsPerPatient: Map<Int, List<NCRRecord>> = ncrRecords.groupBy { it.identification.keyNkr }

        return recordsPerPatient.entries.map { createPatientRecord(it.value) }
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

        return when (val sex = sexes[0]) {
            1 -> Sex.MALE
            2 -> Sex.FEMALE
            else -> throw IllegalStateException("Cannot convert sex: $sex")
        }
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
        return recordsPerTumor.entries.associate { createEpisodesForOneTumorOfInterest(it.value) }
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
        TODO("Not yet implemented")
    }

    private fun diagnosisEpisodes(ncrRecords: List<NCRRecord>): List<NCRRecord> {
        return ncrRecords.filter { it.identification.epis == "DIA" }
    }
}