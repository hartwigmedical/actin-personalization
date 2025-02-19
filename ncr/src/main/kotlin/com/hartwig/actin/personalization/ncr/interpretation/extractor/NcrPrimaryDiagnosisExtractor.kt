package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.diagnosis.PrimaryDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorBasisOfDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocation
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrAnorectalVergeDistanceCategoryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrBooleanMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorTypeMapper
import com.hartwig.actin.personalization.ncr.util.NcrFunctions

object NcrPrimaryDiagnosisExtractor {

    fun extract(records: List<NcrRecord>): PrimaryDiagnosis {
        val diagnosis = NcrFunctions.diagnosisRecord(records)

        return PrimaryDiagnosis(
            basisOfDiagnosis = TumorBasisOfDiagnosis.HISTOLOGICAL_CONFIRMATION,
            hasDoublePrimaryTumor = null,
            primaryTumorType = NcrTumorTypeMapper.resolve(diagnosis.primaryDiagnosis.morfCat!!),
            primaryTumorLocation = TumorLocation.DESCENDING_COLON,
            differentiationGrade = null,

            clinicalTnmClassification = null,
            pathologicalTnmClassification = null,
            clinicalTumorStage = null,
            pathologicalTumorStage = null,
            investigatedLymphNodesCount = null,
            positiveLymphNodesCount = null,

            venousInvasionDescription = null,
            lymphaticInvasionCategory = null,
            extraMuralInvasionCategory = null,
            tumorRegression = null,

            sidedness = null,
            presentedWithIleus = NcrBooleanMapper.resolve(diagnosis.clinicalCharacteristics.ileus),
            presentedWithPerforation = NcrBooleanMapper.resolve(diagnosis.clinicalCharacteristics.perforatie),

            anorectalVergeDistanceCategory = NcrAnorectalVergeDistanceCategoryMapper.resolve(diagnosis.clinicalCharacteristics.anusAfst),
            mesorectalFasciaIsClear = null,
            distanceToMesorectalFasciaMm = null
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
//
//            Diagnosis(
//                tumorLocations = locations,
    //            )
//        }
    }
}