package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.diagnosis.PrimaryDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.Sidedness
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmClassification
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocation
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrAnorectalVergeDistanceCategoryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrBasisOfDiagnosisMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrBooleanMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrDifferentiationGradeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTnmMMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTnmNMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTnmTMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorLocationMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorStageMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorTypeMapper
import com.hartwig.actin.personalization.ncr.util.NcrFunctions

object NcrPrimaryDiagnosisExtractor {

    fun extract(records: List<NcrRecord>): PrimaryDiagnosis {
        val diagnosis = NcrFunctions.diagnosisRecord(records)

        val primaryTumorLocation = NcrTumorLocationMapper.resolveTumorLocation(diagnosis.primaryDiagnosis.topoSublok)

        return PrimaryDiagnosis(
            basisOfDiagnosis = NcrBasisOfDiagnosisMapper.resolve(diagnosis.primaryDiagnosis.diagBasis),
            hasDoublePrimaryTumor = NcrBooleanMapper.resolve(diagnosis.clinicalCharacteristics.dubbeltum)!!,
            primaryTumorType = NcrTumorTypeMapper.resolve(diagnosis.primaryDiagnosis.morfCat!!),
            primaryTumorLocation = primaryTumorLocation,
            differentiationGrade = NcrDifferentiationGradeMapper.resolve(diagnosis.primaryDiagnosis.diffgrad),

            clinicalTnmClassification = extractClinicalTnmClassification(diagnosis),
            pathologicalTnmClassification = extractPathologicalTnmClassification(diagnosis),
            clinicalTumorStage = NcrTumorStageMapper.resolve(diagnosis.primaryDiagnosis.cstadium!!),
            pathologicalTumorStage = NcrTumorStageMapper.resolve(diagnosis.primaryDiagnosis.pstadium!!),
            investigatedLymphNodesCount = null,
            positiveLymphNodesCount = null,

            venousInvasionDescription = null,
            lymphaticInvasionCategory = null,
            extraMuralInvasionCategory = null,
            tumorRegression = null,

            sidedness = determineSidedness(primaryTumorLocation),
            presentedWithIleus = NcrBooleanMapper.resolve(diagnosis.clinicalCharacteristics.ileus),
            presentedWithPerforation = NcrBooleanMapper.resolve(diagnosis.clinicalCharacteristics.perforatie),

            anorectalVergeDistanceCategory = NcrAnorectalVergeDistanceCategoryMapper.resolve(diagnosis.clinicalCharacteristics.anusAfst),
            mesorectalFasciaIsClear = null,
            distanceToMesorectalFasciaMm = null
        )
    }

    private fun extractClinicalTnmClassification(record: NcrRecord): TnmClassification {
        return extractTnmClassification(record.primaryDiagnosis.ct, record.primaryDiagnosis.cn, record.primaryDiagnosis.cm)
    }

    private fun extractPathologicalTnmClassification(record: NcrRecord): TnmClassification {
        return extractTnmClassification(record.primaryDiagnosis.pt, record.primaryDiagnosis.pn, record.primaryDiagnosis.pm)
    }

    private fun extractTnmClassification(tCode: String?, nCode: String?, mCode: String?): TnmClassification {
        return TnmClassification(
            tumor = NcrTnmTMapper.resolveNullable(tCode),
            lymphNodes = NcrTnmNMapper.resolveNullable(nCode),
            metastasis = NcrTnmMMapper.resolveNullable(mCode)
        )
    }

    private val LOCATIONS_INDICATING_LEFT_SIDEDNESS =
        setOf(
            TumorLocation.FLEXURA_LIENALIS,
            TumorLocation.DESCENDING_COLON,
            TumorLocation.RECTOSIGMOID,
            TumorLocation.SIGMOID_COLON,
            TumorLocation.RECTUM
        )
    private val LOCATIONS_INDICATING_RIGHT_SIDEDNESS =
        setOf(TumorLocation.APPENDIX, TumorLocation.COECUM, TumorLocation.ASCENDING_COLON, TumorLocation.FLEXURA_HEPATICA)

    private fun determineSidedness(location: TumorLocation): Sidedness? {
        val containsLeft = location in LOCATIONS_INDICATING_LEFT_SIDEDNESS
        val containsRight = location in LOCATIONS_INDICATING_RIGHT_SIDEDNESS

        return when {
            containsLeft && !containsRight -> Sidedness.LEFT
            containsRight && !containsLeft -> Sidedness.RIGHT
            else -> null
        }
    }
}