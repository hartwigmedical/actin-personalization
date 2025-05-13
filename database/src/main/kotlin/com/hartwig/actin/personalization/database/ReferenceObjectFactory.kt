package com.hartwig.actin.personalization.database

import com.hartwig.actin.personalization.database.datamodel.ReferenceObject
import com.hartwig.actin.personalization.datamodel.ReferenceEntry
import com.hartwig.actin.personalization.datamodel.assessment.AsaClassification

object ReferenceObjectFactory {

    fun create(entry: ReferenceEntry): ReferenceObject {
        val sortedMetastases = entry.metastaticDiagnosis.metastases.sortedBy { metastasis -> metastasis.daysSinceDiagnosis }
        val earliestDistantMetastasisDetectionDays = sortedMetastases.firstOrNull()?.daysSinceDiagnosis
//        val systemicTreatmentPostMetastasis =
//            findSystemicTreatmentPostMetastasis(tumor.treatmentEpisodes, earliestDistantMetastasisDetectionDays)
        
        return ReferenceObject(
            source = entry.source,
            sourceId = entry.sourceId,
            diagnosisYear = entry.diagnosisYear,
            ageAtDiagnosis = entry.ageAtDiagnosis,
            sex = entry.sex,
            
            hadSurvivalEvent = false,
            survivalDaysSincePrimaryDiagnosis = 0,
            survivalDaysSinceMetastaticDiagnosis = 0,
            survivalDaysSinceTreatmentStart = 0,

            numberOfPriorTumors = entry.priorTumors.size,
            hasDoublePrimaryTumor = entry.primaryDiagnosis.hasDoublePrimaryTumor,
            
            basisOfDiagnosis = entry.primaryDiagnosis.basisOfDiagnosis,
            primaryTumorType = entry.primaryDiagnosis.primaryTumorType,
            primaryTumorLocation = entry.primaryDiagnosis.primaryTumorLocation,
            sidedness = entry.primaryDiagnosis.sidedness,
            anorectalVergeDistanceCategory = entry.primaryDiagnosis.anorectalVergeDistanceCategory,
            mesorectalFasciaIsClear = entry.primaryDiagnosis.mesorectalFasciaIsClear,
            distanceToMesorectalFasciaMm = entry.primaryDiagnosis.distanceToMesorectalFasciaMm,
            differentiationGrade = entry.primaryDiagnosis.differentiationGrade,
            clinicalTnmT = entry.primaryDiagnosis.clinicalTnmClassification.tnmT,
            clinicalTnmN = entry.primaryDiagnosis.clinicalTnmClassification.tnmN,
            clinicalTnmM = entry.primaryDiagnosis.clinicalTnmClassification.tnmM,
            pathologicalTnmT = entry.primaryDiagnosis.pathologicalTnmClassification.tnmT,
            pathologicalTnmN = entry.primaryDiagnosis.pathologicalTnmClassification.tnmN,
            pathologicalTnmM = entry.primaryDiagnosis.pathologicalTnmClassification.tnmM,
            clinicalTumorStage = entry.primaryDiagnosis.clinicalTumorStage,
            pathologicalTumorStage = entry.primaryDiagnosis.pathologicalTumorStage,
            investigatedLymphNodesCountPrimaryDiagnosis = entry.primaryDiagnosis.investigatedLymphNodesCount,
            positiveLymphNodesCountPrimaryDiagnosis = entry.primaryDiagnosis.positiveLymphNodesCount,
            presentedWithIleus = entry.primaryDiagnosis.presentedWithIleus,
            presentedWithPerforation = entry.primaryDiagnosis.presentedWithPerforation,
            venousInvasionDescription = entry.primaryDiagnosis.venousInvasionDescription,
            lymphaticInvasionCategory = entry.primaryDiagnosis.lymphaticInvasionCategory,
            extraMuralInvasionCategory = entry.primaryDiagnosis.extraMuralInvasionCategory,
            tumorRegression = entry.primaryDiagnosis.tumorRegression,
            
            daysBetweenPrimaryAndMetastaticDiagnosis = 0,
            hasLiverOrIntrahepaticBileDuctMetastases = false,
            numberOfLiverMetastases = entry.metastaticDiagnosis.numberOfLiverMetastases,
            maximumSizeOfLiverMetastasisMm = entry.metastaticDiagnosis.maximumSizeOfLiverMetastasisMm,
            hasLymphNodeMetastases = false,
            investigatedLymphNodesCountMetastaticDiagnosis = entry.metastaticDiagnosis.investigatedLymphNodesCount,
            positiveLymphNodesCountMetastaticDiagnosis = entry.metastaticDiagnosis.positiveLymphNodesCount,
            hasPeritonealMetastases = false,
            hasBronchusOrLungMetastases = false,
            hasBrainMetastases = false,
            hasOtherMetastases = false,
            
            whoAssessmentAtMetastaticDiagnosis = 0,
            asaAssessmentAtMetastaticDiagnosis = AsaClassification.I,
            lactateDehydrogenaseAtMetastaticDiagnosis = 0.0,
            alkalinePhosphataseAtMetastaticDiagnosis = 0.0,
            leukocytesAbsoluteAtMetastaticDiagnosis = 0.0,
            carcinoembryonicAntigenAtMetastaticDiagnosis = 0.0,
            albumineAtMetastaticDiagnosis = 0.0,
            neutrophilsAbsoluteAtMetastaticDiagnosis = 0.0,
            
            hasHadPrimarySurgeryPriorToMetastaticDiagnosis = false,
            hasHadPrimarySurgeryAfterMetastaticDiagnosis = false,
            hasHadGastroenterologySurgeryPriorToMetastaticDiagnosis = false,
            hasHadGastroenterologySurgeryAfterMetastaticDiagnosis = false,
            hasHadHipecPriorToMetastaticDiagnosis = false,
            hasHadHipecAfterMetastaticDiagnosis = false,
            hasHadPrimaryRadiotherapyPriorToMetastaticDiagnosis = false,
            hasHadPrimaryRadiotherapyAfterMetastaticDiagnosis = false,
            
            hasHadMetastaticSurgery = false,
            hasHadMetastaticRadiotherapy = false,

            hasHadSystemicTreatmentPriorToMetastaticDiagnosis = false,
            daysBetweenMetastaticDiagnosisAndTreatmentStart = 0,
            systemicTreatmentAfterMetastaticDiagnosis = "",
            systemicTreatmentDurationDays = 0,
            systemicTreatmentDurationCycles = 0,
            hadProgressionEvent = false,
            daysBetweenTreatmentStartAndProgression = 0
        )
    }
}