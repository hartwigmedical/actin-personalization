package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.PriorTumor
import com.hartwig.actin.personalization.datamodel.TumorEpisodes
import com.hartwig.actin.personalization.datamodel.TumorOfInterest
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.DIAGNOSIS_EPISODE
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrBooleanMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrLocationMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrStageTnmMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTreatmentNameMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorLocationCategoryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorTypeMapper

fun extractTumorOfInterest(ncrRecords: List<NcrRecord>, tumorEpisodes: TumorEpisodes): TumorOfInterest {
    val episodes = tumorEpisodes.followupEpisodes + tumorEpisodes.diagnosisEpisode

    val diagnosisRecord = ncrRecords.filter { it.identification.epis == DIAGNOSIS_EPISODE }.minBy { it.identification.keyEid }
    val priorTumors = extractPriorTumors(diagnosisRecord)

    return TumorOfInterest(
        consolidatedTumorType = NcrTumorTypeMapper.resolve(ncrRecords.mapNotNull { it.primaryDiagnosis.morfCat }.distinct().single()),
        consolidatedTumorLocation = episodes.map(Episode::tumorLocation).distinct().single(),
        hasHadTumorDirectedSystemicTherapy = episodes.any(Episode::hasReceivedTumorDirectedTreatment),
        hasHadPriorTumor = priorTumors.isNotEmpty(),
        priorTumors = priorTumors
    )
}

private fun extractPriorTumors(record: NcrRecord): List<PriorTumor> {
    return with(record.priorMalignancies) {
        listOfNotNull(
            mal1Morf?.let {
                extractPriorTumor(
                    it,
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
                )
            },
            mal2Morf?.let {
                extractPriorTumor(
                    it,
                    mal2TopoSublok,
                    mal2Syst,
                    mal2Int,
                    2,
                    mal2Tumsoort,
                    mal2Stadium,
                    listOfNotNull(mal2SystCode1, mal2SystCode2, mal2SystCode3, mal2SystCode4, mal2SystCode5)
                )
            },
            mal3Morf?.let {
                extractPriorTumor(
                    it,
                    mal3TopoSublok,
                    mal3Syst,
                    mal3Int,
                    3,
                    mal3Tumsoort,
                    mal3Stadium,
                    listOfNotNull(mal3SystCode1, mal3SystCode2, mal3SystCode3, mal3SystCode4)
                )
            },
            mal4Morf?.let {
                extractPriorTumor(
                    it, mal4TopoSublok, mal4Syst, mal4Int, 4, mal4Tumsoort, mal4Stadium, emptyList()
                )
            }
        )
    }
}

private fun extractPriorTumor(
    type: Int,
    location: String?,
    hadSystemic: Int?,
    interval: Int?,
    id: Int,
    category: Int?,
    stage: String?,
    treatments: List<String>
): PriorTumor {
    if (location == null || category == null) {
        throw IllegalStateException("Missing location information for prior tumor with ID $id")
    }
    return PriorTumor(
        consolidatedTumorType = NcrTumorTypeMapper.resolve(type),
        consolidatedTumorLocation = NcrLocationMapper.resolveLocation(location),
        hasHadTumorDirectedSystemicTherapy = NcrBooleanMapper.resolve(hadSystemic) ?: false,
        incidenceIntervalPrimaryTumor = interval,
        tumorPriorId = id,
        tumorLocationCategory = NcrTumorLocationCategoryMapper.resolve(category),
        stageTNM = NcrStageTnmMapper.resolveNullable(stage),
        systemicTreatments = treatments.map(NcrTreatmentNameMapper::resolve)
    )
}
