package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.diagnosis.PrimaryDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmClassification
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrAnorectalVergeDistanceCategoryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrBasisOfDiagnosisMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrBooleanMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrDifferentiationGradeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTnmMMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTnmNMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTnmTMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorLocationMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorTypeMapper
import com.hartwig.actin.personalization.ncr.util.NcrFunctions

object NcrPrimaryDiagnosisExtractor {

    fun extract(records: List<NcrRecord>): PrimaryDiagnosis {
        val diagnosis = NcrFunctions.diagnosisRecord(records)

        return PrimaryDiagnosis(
            basisOfDiagnosis = NcrBasisOfDiagnosisMapper.resolve(diagnosis.primaryDiagnosis.diagBasis),
            hasDoublePrimaryTumor = NcrBooleanMapper.resolve(diagnosis.clinicalCharacteristics.dubbeltum)!!,
            primaryTumorType = NcrTumorTypeMapper.resolve(diagnosis.primaryDiagnosis.morfCat!!),
            primaryTumorLocation = NcrTumorLocationMapper.resolveTumorLocation(diagnosis.primaryDiagnosis.topoSublok),
            differentiationGrade = NcrDifferentiationGradeMapper.resolve(diagnosis.primaryDiagnosis.diffgrad),

            clinicalTnmClassification = extractClinicalTnmClassification(diagnosis),
            pathologicalTnmClassification = extractPathologicalTnmClassification(diagnosis),
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
}