package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.diagnosis.PriorTumor
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrDrugMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorLocationCategoryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorLocationMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorStageMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorTypeMapper
import com.hartwig.actin.personalization.ncr.util.NcrFunctions

object NcrPriorTumorExtractor {

    fun extract(records: List<NcrRecord>): List<PriorTumor> {
        val diagnosisRecord = NcrFunctions.diagnosisRecord(records)

        return with(diagnosisRecord.priorMalignancies) {
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
        return daysSinceDiagnosis?.let {
            if (primaryTumorTypeCode == null || primaryTumorLocationCode == null || primaryTumorLocationCategoryCode == null) {
                throw IllegalStateException("Missing location information for prior tumor with ID $id")
            }
            PriorTumor(
                daysBeforeDiagnosis = -1 * daysSinceDiagnosis,
                primaryTumorType = NcrTumorTypeMapper.resolve(primaryTumorTypeCode),
                primaryTumorLocation = NcrTumorLocationMapper.resolveTumorLocation(primaryTumorLocationCode),
                primaryTumorLocationCategory = NcrTumorLocationCategoryMapper.resolve(primaryTumorLocationCategoryCode),
                primaryTumorStage = NcrTumorStageMapper.resolveNullable(primaryTumorStageCode),
                systemicDrugsReceived = drugs.map(NcrDrugMapper::resolve)
            )
        }
    }
}