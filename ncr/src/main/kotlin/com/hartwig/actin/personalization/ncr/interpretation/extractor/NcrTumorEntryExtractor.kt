package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.Diagnosis
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.PriorTumor
import com.hartwig.actin.personalization.datamodel.TumorEntry
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.DIAGNOSIS_EPISODE
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrAnorectalVergeDistanceCategoryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrBooleanMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrCciNumberOfCategoriesMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrLocationMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrStageTnmMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTreatmentNameMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorLocationCategoryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorTypeMapper

class NcrTumorEntryExtractor(private val episodeExtractor: NcrEpisodeExtractor) {

    fun extractTumorEntry(records: List<NcrRecord>): TumorEntry {
        val diagnosisRecord = records.single { it.identification.epis == DIAGNOSIS_EPISODE }
        val intervalTumorIncidenceLatestAliveStatus = diagnosisRecord.patientCharacteristics.vitStatInt!!
        val episodes = records.map { record -> episodeExtractor.extractEpisode(record, intervalTumorIncidenceLatestAliveStatus) }
        val locations = episodes.map(Episode::tumorLocation).toSet()
        val priorTumors = extractPriorTumors(diagnosisRecord)

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
                consolidatedTumorType = NcrTumorTypeMapper.resolve(diagnosisRecord.primaryDiagnosis.morfCat!!),
                tumorLocations = locations,
                hasHadTumorDirectedSystemicTherapy = episodes.any(Episode::hasReceivedTumorDirectedTreatment),
                ageAtDiagnosis = diagnosisRecord.patientCharacteristics.leeft,
                intervalTumorIncidenceLatestAliveStatusDays = intervalTumorIncidenceLatestAliveStatus,
                hasHadPriorTumor = priorTumors.isNotEmpty(),
                priorTumors = priorTumors,
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
        return TumorEntry(diagnosis, episodes)
    }

    private fun extractPriorTumors(record: NcrRecord): List<PriorTumor> {
        return with(record.priorMalignancies) {
            listOfNotNull(
                extractPriorTumor(
                    mal1Morf,
                    mal1TopoSublok,
                    mal1Syst,
                    mal1Int,
                    1,
                    mal1Tumsoort,
                    mal1Stadium,
                    listOfNotNull(
                        mal1SystCode1,
                        mal1SystCode2,
                        mal1SystCode3,
                        mal1SystCode4,
                        mal1SystCode5,
                        mal1SystCode6,
                        mal1SystCode7,
                        mal1SystCode8,
                        mal1SystCode9
                    )
                ),
                extractPriorTumor(
                    mal2Morf,
                    mal2TopoSublok,
                    mal2Syst,
                    mal2Int,
                    2,
                    mal2Tumsoort,
                    mal2Stadium,
                    listOfNotNull(mal2SystCode1, mal2SystCode2, mal2SystCode3, mal2SystCode4, mal2SystCode5)
                ),
                extractPriorTumor(
                    mal3Morf,
                    mal3TopoSublok,
                    mal3Syst,
                    mal3Int,
                    3,
                    mal3Tumsoort,
                    mal3Stadium,
                    listOfNotNull(mal3SystCode1, mal3SystCode2, mal3SystCode3, mal3SystCode4)
                ),
                extractPriorTumor(
                    mal4Morf, mal4TopoSublok, mal4Syst, mal4Int, 4, mal4Tumsoort, mal4Stadium, emptyList()
                )
            )
        }
    }

    private fun extractPriorTumor(
        type: Int?,
        location: String?,
        hadSystemic: Int?,
        interval: Int?,
        id: Int,
        category: Int?,
        stage: String?,
        treatments: List<String>
    ): PriorTumor? {
        return type?.let {
            if (location == null || category == null) {
                throw IllegalStateException("Missing location information for prior tumor with ID $id")
            }
            PriorTumor(
                consolidatedTumorType = NcrTumorTypeMapper.resolve(type),
                tumorLocations = setOf(NcrLocationMapper.resolveLocation(location)),
                hasHadTumorDirectedSystemicTherapy = NcrBooleanMapper.resolve(hadSystemic) == true,
                intervalTumorIncidencePriorTumorDays = interval,
                tumorPriorId = id,
                tumorLocationCategory = NcrTumorLocationCategoryMapper.resolve(category),
                stageTNM = NcrStageTnmMapper.resolveNullable(stage),
                systemicTreatments = treatments.map(NcrTreatmentNameMapper::resolve)
            )
        }
    }
}