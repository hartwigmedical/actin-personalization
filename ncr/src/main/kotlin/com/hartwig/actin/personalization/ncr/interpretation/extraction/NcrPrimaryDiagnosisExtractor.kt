package com.hartwig.actin.personalization.ncr.interpretation.extraction

import com.hartwig.actin.personalization.datamodel.diagnosis.PrimaryDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.Sidedness
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmClassification
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocation
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.NcrFunctions
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrAnorectalVergeDistanceCategoryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrBasisOfDiagnosisMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrBooleanMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrDifferentiationGradeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrExtraMuralInvasionCategoryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrLymphaticInvasionCategoryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrTnmMMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrTnmNMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrTnmTMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrTumorLocationMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrTumorRegressionMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrTumorStageMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrTumorTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrVenousInvasionDescriptionMapper

object NcrPrimaryDiagnosisExtractor {

    fun extract(records: List<NcrRecord>): PrimaryDiagnosis {
        val diagnosis = NcrFunctions.diagnosisRecord(records)

        val primaryTumorLocation = NcrTumorLocationMapper.resolveTumorLocation(diagnosis.primaryDiagnosis.topoSublok)
        val (distanceToMesorectalFasciaMm, mesorectalFasciaIsClear) =
            extractDistanceToMesorectalFascia(diagnosis.clinicalCharacteristics.mrfAfst)

        return PrimaryDiagnosis(
            basisOfDiagnosis = NcrBasisOfDiagnosisMapper.resolve(diagnosis.primaryDiagnosis.diagBasis),
            hasDoublePrimaryTumor = NcrBooleanMapper.resolve(diagnosis.clinicalCharacteristics.dubbeltum)!!,

            primaryTumorType = NcrTumorTypeMapper.resolve(diagnosis.primaryDiagnosis.morfCat!!),
            primaryTumorLocation = primaryTumorLocation,
            sidedness = determineSidedness(primaryTumorLocation),
            anorectalVergeDistanceCategory = NcrAnorectalVergeDistanceCategoryMapper.resolve(diagnosis.clinicalCharacteristics.anusAfst),
            mesorectalFasciaIsClear = mesorectalFasciaIsClear,
            distanceToMesorectalFasciaMm = distanceToMesorectalFasciaMm,

            differentiationGrade = NcrDifferentiationGradeMapper.resolve(diagnosis.primaryDiagnosis.diffgrad),
            clinicalTnmClassification = extractClinicalTnmClassification(diagnosis),
            pathologicalTnmClassification = extractPathologicalTnmClassification(diagnosis),
            clinicalTumorStage = NcrTumorStageMapper.resolve(diagnosis.primaryDiagnosis.cstadium!!),
            pathologicalTumorStage = NcrTumorStageMapper.resolve(diagnosis.primaryDiagnosis.pstadium!!),
            investigatedLymphNodesCount = diagnosis.primaryDiagnosis.ondLymf,
            positiveLymphNodesCount = diagnosis.primaryDiagnosis.posLymf,

            presentedWithIleus = NcrBooleanMapper.resolve(diagnosis.clinicalCharacteristics.ileus),
            presentedWithPerforation = NcrBooleanMapper.resolve(diagnosis.clinicalCharacteristics.perforatie),
            venousInvasionDescription = NcrVenousInvasionDescriptionMapper.resolve(diagnosis.clinicalCharacteristics.veneusInvas),
            lymphaticInvasionCategory = NcrLymphaticInvasionCategoryMapper.resolve(diagnosis.clinicalCharacteristics.lymfInvas),
            extraMuralInvasionCategory = NcrExtraMuralInvasionCategoryMapper.resolve(diagnosis.clinicalCharacteristics.emi),
            tumorRegression = NcrTumorRegressionMapper.resolve(diagnosis.clinicalCharacteristics.tumregres)
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
            tnmT = NcrTnmTMapper.resolveNullable(tCode),
            tnmN = NcrTnmNMapper.resolveNullable(nCode),
            tnmM = NcrTnmMMapper.resolveNullable(mCode)
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

    private fun extractDistanceToMesorectalFascia(mrfAfst: Int?): Pair<Int?, Boolean?> {
        return when (mrfAfst) {
            null, 888, 999 -> null to null
            111 -> null to true
            222 -> null to false
            in 0..20 -> mrfAfst to null
            else -> throw IllegalStateException("Unexpected value for distance to mesorectal fascia: $mrfAfst")
        }
    }
}