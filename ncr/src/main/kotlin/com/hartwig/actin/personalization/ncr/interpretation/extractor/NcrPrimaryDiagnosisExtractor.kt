package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.diagnosis.PrimaryDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmClassification
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrAnorectalVergeDistanceCategoryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrBooleanMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorBasisOfDiagnosisMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorDifferentiationGradeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorLocationMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorTypeMapper
import com.hartwig.actin.personalization.ncr.util.NcrFunctions

object NcrPrimaryDiagnosisExtractor {

    fun extract(records: List<NcrRecord>): PrimaryDiagnosis {
        val diagnosis = NcrFunctions.diagnosisRecord(records)

        return PrimaryDiagnosis(
            basisOfDiagnosis = NcrTumorBasisOfDiagnosisMapper.resolve(diagnosis.primaryDiagnosis.diagBasis),
            hasDoublePrimaryTumor = NcrBooleanMapper.resolve(diagnosis.clinicalCharacteristics.dubbeltum)!!,
            primaryTumorType = NcrTumorTypeMapper.resolve(diagnosis.primaryDiagnosis.morfCat!!),
            primaryTumorLocation = NcrTumorLocationMapper.resolveTumorLocation(diagnosis.primaryDiagnosis.topoSublok),
            differentiationGrade = NcrTumorDifferentiationGradeMapper.resolve(diagnosis.primaryDiagnosis.diffgrad),

//            tnmCT = NcrTnmTMapper.resolveNullable(primaryDiagnosis.ct),
//            tnmCN = NcrTnmNMapper.resolveNullable(primaryDiagnosis.cn),
//            tnmCM = NcrTnmMMapper.resolveNullable(primaryDiagnosis.cm),
//            tnmPT = NcrTnmTMapper.resolveNullable(primaryDiagnosis.pt),
//            tnmPN = NcrTnmNMapper.resolveNullable(primaryDiagnosis.pn),
//            tnmPM = NcrTnmMMapper.resolveNullable(primaryDiagnosis.pm),

            clinicalTnmClassification = TnmClassification(null, null, null),
            pathologicalTnmClassification = TnmClassification(null, null, null),
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

//    fun extractTnmClassification(record : NcrRecord) :  {
//        return TnmClassification(
//            NcrTnmTMapper . resolveNullable (primaryDiagnosis.ct),
//        tnmCN = NcrTnmNMapper.resolveNullable(primaryDiagnosis.cn),
//        tnmCM = NcrTnmMMapper.resolveNullable(primaryDiagnosis.cm),
//
//        )
//    }
}