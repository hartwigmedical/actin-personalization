package com.hartwig.actin.personalization.ncr.interpretation.extraction

import com.hartwig.actin.personalization.datamodel.diagnosis.Metastasis
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastaticDiagnosis
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.NcrFunctions
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrBooleanMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrDistantMetastasesStatusMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrNumberOfLiverMetastasesMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrTumorLocationMapper

object NcrMetastaticDiagnosisExtractor {

    fun extract(records: List<NcrRecord>): MetastaticDiagnosis {
        val metastatic = NcrFunctions.metastaticRecord(records)

        return MetastaticDiagnosis(
            distantMetastasesDetectionStatus = NcrDistantMetastasesStatusMapper.resolve(metastatic.identification.metaEpis),
            metastases = extractMetastases(metastatic),
            numberOfLiverMetastases = NcrNumberOfLiverMetastasesMapper.resolve(metastatic.metastaticDiagnosis.metaLeverAantal),
            maximumSizeOfLiverMetastasisMm = metastatic.metastaticDiagnosis.metaLeverAfm.takeIf { it != 999 },
            // TODO (KD): Figure out what these values mean in the context of a metastatic NCR record
            investigatedLymphNodesCount = metastatic.primaryDiagnosis.ondLymf,
            positiveLymphNodesCount = metastatic.primaryDiagnosis.posLymf
        )
    }

    private fun extractMetastases(record: NcrRecord): List<Metastasis> {
        return with(record.metastaticDiagnosis) {
            val locations = listOfNotNull(
                metaTopoSublok1,
                metaTopoSublok2,
                metaTopoSublok3,
                metaTopoSublok4,
                metaTopoSublok5,
                metaTopoSublok6,
                metaTopoSublok7,
                metaTopoSublok8,
                metaTopoSublok9,
                metaTopoSublok10
            )
            val daysSinceDiagnosis = listOf(
                metaInt1,
                metaInt2,
                metaInt3,
                metaInt4,
                metaInt5,
                metaInt6,
                metaInt7,
                metaInt8,
                metaInt9,
                metaInt10
            )
            val isLinkedToProgression = listOf(
                metaProg1,
                metaProg2,
                metaProg3,
                metaProg4,
                metaProg5,
                metaProg6,
                metaProg7,
                metaProg8,
                metaProg9,
                metaProg10
            )
            locations.mapIndexed { i, location ->
                Metastasis(
                    daysSinceDiagnosis = daysSinceDiagnosis[i],
                    location = NcrTumorLocationMapper.resolveTumorLocation(location),
                    isLinkedToProgression = NcrBooleanMapper.resolve(isLinkedToProgression[i])
                )
            }
        }
    }
}