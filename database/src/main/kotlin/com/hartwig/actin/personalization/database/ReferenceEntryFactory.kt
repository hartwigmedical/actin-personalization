package com.hartwig.actin.personalization.database

import com.hartwig.actin.personalization.database.datamodel.ReferenceEntry
import com.hartwig.actin.personalization.datamodel.ReferencePatient
import com.hartwig.actin.personalization.datamodel.Tumor
import com.hartwig.actin.personalization.datamodel.assessment.AsaClassification

object ReferenceEntryFactory {

    fun create(patient: ReferencePatient, tumor: Tumor): ReferenceEntry {
        val sortedMetastases = tumor.metastaticDiagnosis.metastases.sortedBy { metastasis -> metastasis.daysSinceDiagnosis }
        val earliestDistantMetastasisDetectionDays = sortedMetastases.firstOrNull()?.daysSinceDiagnosis
//        val systemicTreatmentPostMetastasis =
//            findSystemicTreatmentPostMetastasis(tumor.treatmentEpisodes, earliestDistantMetastasisDetectionDays)
        
        return ReferenceEntry(
            source = patient.source,
            sourceId = patient.sourceId,
            diagnosisYear = tumor.diagnosisYear,
            ageAtDiagnosis = tumor.ageAtDiagnosis,
            sex = patient.sex,
            
            hadSurvivalEvent = false,
            survivalDaysSincePrimaryDiagnosis = 0,
            survivalDaysSinceMetastaticDiagnosis = 0,
            survivalDaysSinceTreatmentStart = 0,

            numberOfPriorTumors = tumor.priorTumors.size,
            hasDoublePrimaryTumor = tumor.primaryDiagnosis.hasDoublePrimaryTumor,
            
            basisOfDiagnosis = tumor.primaryDiagnosis.basisOfDiagnosis,
            primaryTumorType = tumor.primaryDiagnosis.primaryTumorType,
            primaryTumorLocation = tumor.primaryDiagnosis.primaryTumorLocation,
            sidedness = tumor.primaryDiagnosis.sidedness,
            anorectalVergeDistanceCategory = tumor.primaryDiagnosis.anorectalVergeDistanceCategory,
            mesorectalFasciaIsClear = tumor.primaryDiagnosis.mesorectalFasciaIsClear,
            distanceToMesorectalFasciaMm = tumor.primaryDiagnosis.distanceToMesorectalFasciaMm,
            differentiationGrade = tumor.primaryDiagnosis.differentiationGrade,
            clinicalTnmT = tumor.primaryDiagnosis.clinicalTnmClassification.tnmT,
            clinicalTnmN = tumor.primaryDiagnosis.clinicalTnmClassification.tnmN,
            clinicalTnmM = tumor.primaryDiagnosis.clinicalTnmClassification.tnmM,
            pathologicalTnmT = tumor.primaryDiagnosis.pathologicalTnmClassification.tnmT,
            pathologicalTnmN = tumor.primaryDiagnosis.pathologicalTnmClassification.tnmN,
            pathologicalTnmM = tumor.primaryDiagnosis.pathologicalTnmClassification.tnmM,
            clinicalTumorStage = tumor.primaryDiagnosis.clinicalTumorStage,
            pathologicalTumorStage = tumor.primaryDiagnosis.pathologicalTumorStage,
            investigatedLymphNodesCountPrimaryDiagnosis = tumor.primaryDiagnosis.investigatedLymphNodesCount,
            positiveLymphNodesCountPrimaryDiagnosis = tumor.primaryDiagnosis.positiveLymphNodesCount,
            presentedWithIleus = tumor.primaryDiagnosis.presentedWithIleus,
            presentedWithPerforation = tumor.primaryDiagnosis.presentedWithPerforation,
            venousInvasionDescription = tumor.primaryDiagnosis.venousInvasionDescription,
            lymphaticInvasionCategory = tumor.primaryDiagnosis.lymphaticInvasionCategory,
            extraMuralInvasionCategory = tumor.primaryDiagnosis.extraMuralInvasionCategory,
            tumorRegression = tumor.primaryDiagnosis.tumorRegression,
            
            daysBetweenPrimaryAndMetastaticDiagnosis = 0,
            hasLiverOrIntrahepaticBileDuctMetastases = false,
            numberOfLiverMetastases = tumor.metastaticDiagnosis.numberOfLiverMetastases,
            maximumSizeOfLiverMetastasisMm = tumor.metastaticDiagnosis.maximumSizeOfLiverMetastasisMm,
            hasLymphNodeMetastases = false,
            investigatedLymphNodesCountMetastaticDiagnosis = tumor.metastaticDiagnosis.investigatedLymphNodesCount,
            positiveLymphNodesCountMetastaticDiagnosis = tumor.metastaticDiagnosis.positiveLymphNodesCount,
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