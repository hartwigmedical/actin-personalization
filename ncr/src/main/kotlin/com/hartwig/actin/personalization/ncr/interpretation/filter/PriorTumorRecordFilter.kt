package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.FOLLOW_UP_EPISODE
import kotlin.reflect.full.memberProperties

class PriorTumorRecordFilter(override val logFilteredRecords: Boolean) : RecordFilter {

    inline fun <reified T : Any> allFieldsAreNull(obj: T): Boolean {
        return T::class.memberProperties.all { prop ->
            prop.get(obj) == null
        }
    }

    internal fun hasEmptyPriorTumorInVerbEpisode(tumorRecords: List<NcrRecord>): Boolean {
        val verbRecords = tumorRecords.filter { it.identification.epis == FOLLOW_UP_EPISODE }

        val hasEmptyPriorTumor = verbRecords.all { allFieldsAreNull(it.priorMalignancies) }
        if (!hasEmptyPriorTumor) {
            log("Non-empty prior tumor data found for verb records of tumor ID ${tumorRecords.tumorId()}")
        }
        return hasEmptyPriorTumor
    }

    internal fun hasNoPositiveValueInMalInt(tumorRecords: List<NcrRecord>): Boolean {
        val allMalIntValues = tumorRecords.flatMap { it ->
            listOf(
                it.priorMalignancies.mal1Int,
                it.priorMalignancies.mal2Int,
                it.priorMalignancies.mal3Int,
                it.priorMalignancies.mal4Int
            )
        }.filterNotNull()
        val hasNoPositiveValues = allMalIntValues.all { it <= 0 }
        if (!hasNoPositiveValues) {
            log("Found positive malInt values for tumor ID ${tumorRecords.tumorId()}: $allMalIntValues")
        }
        return hasNoPositiveValues
    }

    internal fun hasCompletePriorTumorData(tumorRecords: List<NcrRecord>): Boolean {
        val allPriorTumorInfo = tumorRecords.map { it -> it.priorMalignancies }
            .flatMap { it ->
                listOf(
                    listOf(it.mal1Int, it.mal1Morf, it.mal1TopoSublok, it.mal1Tumsoort, it.mal1Syst),
                    listOf(it.mal2Int, it.mal2Morf, it.mal2TopoSublok, it.mal2Tumsoort, it.mal2Syst),
                    listOf(it.mal3Int, it.mal3Morf, it.mal3TopoSublok, it.mal3Tumsoort, it.mal3Syst),
                    listOf(it.mal4Int, it.mal4Morf, it.mal4TopoSublok, it.mal4Tumsoort, it.mal4Syst)
                )
            }
        val allNullOrComplete =
            allPriorTumorInfo.all { priorMalignancy -> priorMalignancy.all { it == null } || priorMalignancy.none { it == null } }
        if (!allNullOrComplete) {
            log("Incomplete prior tumor data found for tumor ID ${tumorRecords.tumorId()}: $allPriorTumorInfo")
        }
        return allNullOrComplete
    }

    override fun tumorRecords(record: List<NcrRecord>): Boolean {
        return listOf(
            ::hasEmptyPriorTumorInVerbEpisode,
            ::hasNoPositiveValueInMalInt,
            ::hasCompletePriorTumorData
        ).all { it(record) }
    }
}