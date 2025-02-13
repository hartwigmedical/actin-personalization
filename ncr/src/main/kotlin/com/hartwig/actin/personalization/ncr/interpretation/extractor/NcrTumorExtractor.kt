package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.Tumor
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastasesDetectionStatus
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastaticDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.PrimaryDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.PriorTumor
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorBasisOfDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocation
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorType
import com.hartwig.actin.personalization.datamodel.outcome.SurvivalMeasure
import com.hartwig.actin.personalization.datamodel.treatment.HipecTreatment
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.DIAGNOSIS_EPISODE
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrDrugMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorLocationCategoryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorLocationMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorStageMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrVitalStatusMapper

class NcrTumorExtractor(private val episodeExtractor: NcrEpisodeExtractor) {

    fun extractTumor(records: List<NcrRecord>): Tumor {
        val diagnosisRecord = records.single { it.identification.epis == DIAGNOSIS_EPISODE }

        return Tumor(
            diagnosisYear = diagnosisRecord.primaryDiagnosis.incjr,
            ageAtDiagnosis = diagnosisRecord.patientCharacteristics.leeft,
            latestSurvivalStatus = SurvivalMeasure(
                daysSinceDiagnosis = diagnosisRecord.patientCharacteristics.vitStatInt!!,
                isAlive = NcrVitalStatusMapper.resolve(diagnosisRecord.patientCharacteristics.vitStat!!)
            ),
            priorTumors = extractPriorTumors(diagnosisRecord),
            primaryDiagnosis = PrimaryDiagnosis(
                basisOfDiagnosis = TumorBasisOfDiagnosis.HISTOLOGICAL_CONFIRMATION,
                primaryTumorType = TumorType.ADENOCARCINOMA_DIFFUSE_TYPE,
                primaryTumorLocation = TumorLocation.DESCENDING_COLON
            ),
            metastaticDiagnosis = MetastaticDiagnosis(
                distantMetastasesDetectionStatus = MetastasesDetectionStatus.AT_START,
                metastases = listOf()
            ),
            hasReceivedTumorDirectedTreatment = false,
            hipecTreatment = HipecTreatment(daysSinceDiagnosis = null, hasHadHipecTreatment = false)
        )


//        val episodes = records.map { record ->
//            episodeExtractor.extractEpisode(record, intervalTumorIncidenceLatestAliveStatus)
//        }.sortedBy(Episode::order)
//
//        val orderOfFirstDistantMetastasesEpisode = episodes.firstOrNull { episode ->
//            episode.distantMetastasesDetectionStatus in setOf(
//                MetastasesDetectionStatus.AT_START,
//                MetastasesDetectionStatus.AT_PROGRESSION
//            )
//        }?.order ?: throw IllegalStateException("orderOfFirstDistantMetastasesEpisode is not allowed to be null")
//        val locations = episodes.map(Episode::tumorLocation).toSet()
//        val priorTumors = extractPriorTumors(diagnosisRecord)
//
//        val diagnosis = with(diagnosisRecord) {
//            val (hasBrafMutation, hasBrafV600EMutation) = when (molecularCharacteristics.brafMut) {
//                0 -> Pair(false, false)
//                1 -> Pair(true, null)
//                2 -> Pair(true, true)
//                3 -> Pair(true, false)
//                9, null -> Pair(null, null)
//                else -> throw IllegalStateException("Unexpected value for BRAF mutation: ${molecularCharacteristics.brafMut}")
//            }
//            val (hasRasMutation, hasKrasG12CMutation) = when (molecularCharacteristics.rasMut) {
//                0 -> Pair(false, false)
//                1 -> Pair(true, null)
//                2 -> Pair(true, false)
//                3 -> Pair(true, true)
//                9, null -> Pair(null, null)
//                else -> throw IllegalStateException("Unexpected value for RAS mutation: ${molecularCharacteristics.rasMut}")
//            }
//
//            Diagnosis(
//                consolidatedTumorType = NcrTumorTypeMapper.resolve(diagnosisRecord.primaryDiagnosis.morfCat!!),
//                tumorLocations = locations,
//                hasHadTumorDirectedSystemicTherapy = episodes.any(Episode::hasReceivedTumorDirectedTreatment),
//                ageAtDiagnosis = patientCharacteristics.leeft,
//                observedOsFromTumorIncidenceDays = intervalTumorIncidenceLatestAliveStatus,
//                hadSurvivalEvent = patientCharacteristics.vitStat!! == 1,
//                hasHadPriorTumor = priorTumors.isNotEmpty(),
//                priorTumors = priorTumors,
//                orderOfFirstDistantMetastasesEpisode = orderOfFirstDistantMetastasesEpisode,
//                isMetachronous = orderOfFirstDistantMetastasesEpisode > 1,
//                cci = comorbidities.cci,
//                cciNumberOfCategories = NcrCciNumberOfCategoriesMapper.resolve(comorbidities.cciCat),
//                cciHasAids = NcrBooleanMapper.resolve(comorbidities.cciAids),
//                cciHasCongestiveHeartFailure = NcrBooleanMapper.resolve(comorbidities.cciChf),
//                cciHasCollagenosis = NcrBooleanMapper.resolve(comorbidities.cciCollagenosis),
//                cciHasCopd = NcrBooleanMapper.resolve(comorbidities.cciCopd),
//                cciHasCerebrovascularDisease = NcrBooleanMapper.resolve(comorbidities.cciCvd),
//                cciHasDementia = NcrBooleanMapper.resolve(comorbidities.cciDementia),
//                cciHasDiabetesMellitus = NcrBooleanMapper.resolve(comorbidities.cciDm),
//                cciHasDiabetesMellitusWithEndOrganDamage = NcrBooleanMapper.resolve(comorbidities.cciEodDm),
//                cciHasOtherMalignancy = NcrBooleanMapper.resolve(comorbidities.cciMalignancy),
//                cciHasOtherMetastaticSolidTumor = NcrBooleanMapper.resolve(comorbidities.cciMetastatic),
//                cciHasMyocardialInfarct = NcrBooleanMapper.resolve(comorbidities.cciMi),
//                cciHasMildLiverDisease = NcrBooleanMapper.resolve(comorbidities.cciMildLiver),
//                cciHasHemiplegiaOrParaplegia = NcrBooleanMapper.resolve(comorbidities.cciPlegia),
//                cciHasPeripheralVascularDisease = NcrBooleanMapper.resolve(comorbidities.cciPvd),
//                cciHasRenalDisease = NcrBooleanMapper.resolve(comorbidities.cciRenal),
//                cciHasLiverDisease = NcrBooleanMapper.resolve(comorbidities.cciSevereLiver),
//                cciHasUlcerDisease = NcrBooleanMapper.resolve(comorbidities.cciUlcer),
//                presentedWithIleus = NcrBooleanMapper.resolve(clinicalCharacteristics.ileus),
//                presentedWithPerforation = NcrBooleanMapper.resolve(clinicalCharacteristics.perforatie),
//                anorectalVergeDistanceCategory = NcrAnorectalVergeDistanceCategoryMapper.resolve(clinicalCharacteristics.anusAfst),
//                hasMsi = NcrBooleanMapper.resolve(molecularCharacteristics.msiStat),
//                hasBrafMutation = hasBrafMutation,
//                hasBrafV600EMutation = hasBrafV600EMutation,
//                hasRasMutation = hasRasMutation,
//                hasKrasG12CMutation = hasKrasG12CMutation
//            )
//        }
    }

    private fun extractPriorTumors(record: NcrRecord): List<PriorTumor> {
        return with(record.priorMalignancies) {
            listOfNotNull(
                extractPriorTumor(
                    1,
                    mal1Int,
                    mal1Morf,
                    mal1TopoSublok,
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
                    2,
                    mal2Int,
                    mal2Morf,
                    mal2TopoSublok,
                    mal2Tumsoort,
                    mal2Stadium,
                    listOfNotNull(mal2SystCode1, mal2SystCode2, mal2SystCode3, mal2SystCode4, mal2SystCode5)
                ),
                extractPriorTumor(
                    3,
                    mal3Int,
                    mal3Morf,
                    mal3TopoSublok,
                    mal3Tumsoort,
                    mal3Stadium,
                    listOfNotNull(mal3SystCode1, mal3SystCode2, mal3SystCode3, mal3SystCode4)
                ),
                extractPriorTumor(
                    4, mal4Int, mal4Morf, mal4TopoSublok, mal4Tumsoort, mal4Stadium, emptyList()
                )
            )
        }
    }

    private fun extractPriorTumor(
        id: Int,
        daysSinceDiagnosis: Int?,
        primaryTumorTypeCode: Int?,
        primaryTumorLocationCode: String?,
        primaryTumorLocationCategoryCode: Int?,
        primaryTumorStageCode: String?,
        drugs: List<String>
    ): PriorTumor? {
        return primaryTumorTypeCode?.let {
            if (primaryTumorLocationCode == null || primaryTumorLocationCategoryCode == null) {
                throw IllegalStateException("Missing location information for prior tumor with ID $id")
            }
            PriorTumor(
                daysBeforeDiagnosis = daysSinceDiagnosis?.let { -1 * daysSinceDiagnosis },
                primaryTumorType = NcrTumorTypeMapper.resolve(primaryTumorTypeCode),
                primaryTumorLocation = NcrTumorLocationMapper.resolveTumorLocation(primaryTumorLocationCode),
                primaryTumorLocationCategory = NcrTumorLocationCategoryMapper.resolve(primaryTumorLocationCategoryCode),
                primaryTumorStage = NcrTumorStageMapper.resolveNullable(primaryTumorStageCode),
                systemicDrugsReceived = drugs.map(NcrDrugMapper::resolve)
            )
        }
    }
}
