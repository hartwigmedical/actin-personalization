package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.FOLLOW_UP_EPISODE

class PriorTumorRecordFilter(val log: (String) -> Unit) {
    
    internal fun hasEmptyPriorTumorInVerbEpisode(tumorRecordsPerId: Map.Entry<Int, List<NcrRecord>>): Boolean {
        val verbRecords = tumorRecordsPerId.value.filter { it.identification.epis == FOLLOW_UP_EPISODE }

        val hasEmptyPriorTumor = verbRecords.all { areAllFieldsNull(it.priorMalignancies) }
        if (!hasEmptyPriorTumor) {
            log("Non-empty prior tumor data found for verb records of tumor ID ${tumorRecordsPerId.key}")
        }
        return hasEmptyPriorTumor
    }

    internal fun hasNoPositiveValueInMalInt(tumorRecordsPerId: Map.Entry<Int, List<NcrRecord>>): Boolean {
        val allMalIntValues = tumorRecordsPerId.value.map { it ->
            listOf(
                it.priorMalignancies.mal1Int,
                it.priorMalignancies.mal2Int,
                it.priorMalignancies.mal3Int,
                it.priorMalignancies.mal4Int
            )
        }
        val hasNoPositiveValues = allMalIntValues.flatten().filterNotNull().all { it <= 0 }
        return hasNoPositiveValues
    }

    internal fun hasCompletePriorTumorData(tumorRecordsPerId: Map.Entry<Int, List<NcrRecord>>): Boolean {
        val allPriorTumorInfo = tumorRecordsPerId.value.map { it -> it.priorMalignancies }
            .map { it ->
                listOf(
                    listOf(it.mal1Int, it.mal1Morf, it.mal1TopoSublok, it.mal1Tumsoort, it.mal1Syst),
                    listOf(it.mal2Int, it.mal2Morf, it.mal2TopoSublok, it.mal2Tumsoort, it.mal2Syst),
                    listOf(it.mal3Int, it.mal3Morf, it.mal3TopoSublok, it.mal3Tumsoort, it.mal3Syst),
                    listOf(it.mal4Int, it.mal4Morf, it.mal4TopoSublok, it.mal4Tumsoort, it.mal4Syst)
                )
            }.flatten()
        return allPriorTumorInfo.all { priorMalignancy -> priorMalignancy.all { it == null } || priorMalignancy.none { it == null } }
    }
}