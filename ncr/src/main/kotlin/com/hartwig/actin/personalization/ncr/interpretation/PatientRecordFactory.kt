package com.hartwig.actin.personalization.ncr.interpretation

import com.hartwig.actin.personalization.datamodel.Diagnosis
import com.hartwig.actin.personalization.datamodel.PatientRecord
import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.datamodel.TumorEpisodes
import com.hartwig.actin.personalization.datamodel.TumorOfInterest
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.extractor.extractEpisode
import com.hartwig.actin.personalization.ncr.interpretation.extractor.extractTumorOfInterest
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrAnorectalVergeDistanceCategoryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrBooleanMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrCciNumberOfCategoriesMapper
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

    private fun determineEpisodesPerTumorOfInterest(ncrRecords: List<NcrRecord>): Map<TumorOfInterest, TumorEpisodes> {
        val recordsPerTumor = ncrRecords.groupBy { it.identification.keyZid }
        return recordsPerTumor.entries.mapNotNull { (tumorId, records) ->
            createTumorEpisodes(records)?.let { tumorEpisodes -> extractTumorOfInterest(records, tumorEpisodes, tumorId) to tumorEpisodes }
        }.toMap()
    }

    private fun createTumorEpisodes(ncrRecords: List<NcrRecord>): TumorEpisodes? {
        val (ncrDiagnosisRecords, ncrTreatmentRecords) = ncrRecords.partition { it.identification.epis == DIAGNOSIS_EPISODE }
        val diagnosisRecord = ncrDiagnosisRecords.minBy { it.identification.keyEid }
        val diagnosis = with(diagnosisRecord) {
            val (hasBrafMutation, hasBrafV600EMutation) = when (molecularCharacteristics.brafMut) {
                0 -> Pair(false, false)
                1 -> Pair(true, null)
                2 -> Pair(true, true)
                3 -> Pair(true, false)
                9, null -> Pair(null, null)
                else -> throw IllegalStateException("Unexpected value for BRAF mutation: ${molecularCharacteristics.brafMut}")
            }
            val (hasRasMutation, hasKrasG12CMutation) = when (molecularCharacteristics.rasMut) {
                0 -> Pair(false, false)
                1 -> Pair(true, null)
                2 -> Pair(true, false)
                3 -> Pair(true, true)
                9, null -> Pair(null, null)
                else -> throw IllegalStateException("Unexpected value for RAS mutation: ${molecularCharacteristics.rasMut}")
            }

            Diagnosis(
                cci = comorbidities.cci,
                cciNumberOfCategories = NcrCciNumberOfCategoriesMapper.resolve(comorbidities.cciCat),
                cciHasAids = NcrBooleanMapper.resolve(comorbidities.cciAids),
                cciHasCongestiveHeartFailure = NcrBooleanMapper.resolve(comorbidities.cciChf),
                cciHasCollagenosis = NcrBooleanMapper.resolve(comorbidities.cciCollagenosis),
                cciHasCopd = NcrBooleanMapper.resolve(comorbidities.cciCopd),
                cciHasCerebrovascularDisease = NcrBooleanMapper.resolve(comorbidities.cciCvd),
                cciHasDementia = NcrBooleanMapper.resolve(comorbidities.cciDementia),
                cciHasDiabetesMellitus = NcrBooleanMapper.resolve(comorbidities.cciDm),
                cciHasDiabetesMellitusWithEndOrganDamage = NcrBooleanMapper.resolve(comorbidities.cciEodDm),
                cciHasOtherMalignancy = NcrBooleanMapper.resolve(comorbidities.cciMalignancy),
                cciHasOtherMetastaticSolidTumor = NcrBooleanMapper.resolve(comorbidities.cciMetastatic),
                cciHasMyocardialInfarct = NcrBooleanMapper.resolve(comorbidities.cciMi),
                cciHasMildLiverDisease = NcrBooleanMapper.resolve(comorbidities.cciMildLiver),
                cciHasHemiplegiaOrParaplegia = NcrBooleanMapper.resolve(comorbidities.cciPlegia),
                cciHasPeripheralVascularDisease = NcrBooleanMapper.resolve(comorbidities.cciPvd),
                cciHasRenalDisease = NcrBooleanMapper.resolve(comorbidities.cciRenal),
                cciHasLiverDisease = NcrBooleanMapper.resolve(comorbidities.cciSevereLiver),
                cciHasUlcerDisease = NcrBooleanMapper.resolve(comorbidities.cciUlcer),
                presentedWithIleus = NcrBooleanMapper.resolve(clinicalCharacteristics.ileus),
                presentedWithPerforation = NcrBooleanMapper.resolve(clinicalCharacteristics.perforatie),
                anorectalVergeDistanceCategory = NcrAnorectalVergeDistanceCategoryMapper.resolve(clinicalCharacteristics.anusAfst),
                hasMsi = NcrBooleanMapper.resolve(molecularCharacteristics.msiStat),
                hasBrafMutation = hasBrafMutation,
                hasBrafV600EMutation = hasBrafV600EMutation,
                hasRasMutation = hasRasMutation,
                hasKrasG12CMutation = hasKrasG12CMutation
            )
        }

	return extractEpisode(diagnosisRecord)?.let {
            TumorEpisodes(diagnosis, it, ncrTreatmentRecords.mapNotNull(::extractEpisode))
        }
    }

    private fun diagnosisEpisodes(ncrRecords: List<NcrRecord>): List<NcrRecord> {
        return ncrRecords.filter { it.identification.epis == DIAGNOSIS_EPISODE }
    }
}
