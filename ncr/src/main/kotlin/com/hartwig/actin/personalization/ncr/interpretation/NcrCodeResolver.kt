package com.hartwig.actin.personalization.ncr.interpretation

import com.hartwig.actin.personalization.datamodel.*
import com.hartwig.actin.personalization.ncr.interpretation.mapper.*

object NcrCodeResolver {

    inline fun <reified T> resolve(code: Int): T {
        return when (T::class) {
            AnastomoticLeakageAfterSurgery::class -> NcrAnastomoticLeakageAfterSurgeryMapper
            AnorectalVergeDistanceCategory::class -> NcrAnorectalVergeDistanceCategoryMapper
            AsaClassificationPreSurgeryOrEndoscopy::class -> NcrAsaClassificationPreSurgeryOrEndoscopyMapper
            CciNumberOfCategories::class -> NcrCciNumberOfCategoriesMapper
            DistantMetastasesStatus::class -> NcrDistantMetastasesStatusMapper
            ExtraMuralInvasionCategory::class -> NcrExtraMuralInvasionCategoryMapper
            GastroenterologyResectionType::class -> NcrGastroenterologyResectionTypeMapper
            LymphaticInvasionCategory::class -> NcrLymphaticInvasionCategoryMapper
            PFSMeasureType::class -> NcrPFSMeasureTypeMapper
            RadiotherapyType::class -> NcrRadiotherapyTypeMapper
            ReasonRefrainmentFromTumorDirectedTreatment::class -> NcrReasonRefrainmentFromTumorDirectedTherapyMapper
            Sex::class -> NcrSexMapper
            SurgeryCircumferentialResectionMargin::class -> NcrSurgeryCircumferentialResectionMarginMapper
            SurgeryRadicality::class -> NcrSurgeryRadicalityMapper
            SurgeryTechnique::class -> NcrSurgeryTechniqueMapper
            SurgeryType::class -> NcrSurgeryTypeMapper
            SurgeryUrgency::class -> NcrSurgeryUrgencyMapper
            TumorBasisOfDiagnosis::class -> NcrTumorBasisOfDiagnosisMapper
            TumorDifferentiationGrade::class -> NcrTumorDifferentiationGradeMapper
            TumorLocationCategory::class -> NcrTumorLocationCategoryMapper
            TumorRegression::class -> NcrTumorRegressionMapper
            TumorType::class -> NcrTumorTypeMapper
            VenousInvasionCategory::class -> NcrVenousInvasionCategoryMapper
            VitalStatus::class -> NcrVitalStatusMapper
            else -> throw IllegalArgumentException("No resolver found for ${T::class.simpleName}")
        }.resolve(code) as T
    }
}