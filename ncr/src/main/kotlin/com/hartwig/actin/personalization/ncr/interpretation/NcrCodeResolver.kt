package com.hartwig.actin.personalization.ncr.interpretation

import com.hartwig.actin.personalization.datamodel.AnastomoticLeakageAfterSurgery
import com.hartwig.actin.personalization.datamodel.AnorectalVergeDistanceCategory
import com.hartwig.actin.personalization.datamodel.AsaClassificationPreSurgeryOrEndoscopy
import com.hartwig.actin.personalization.datamodel.CciNumberOfCategories
import com.hartwig.actin.personalization.datamodel.DistantMetastasesStatus
import com.hartwig.actin.personalization.datamodel.ExtraMuralInvasionCategory
import com.hartwig.actin.personalization.datamodel.GastroenterologyResectionType
import com.hartwig.actin.personalization.datamodel.LymphaticInvasionCategory
import com.hartwig.actin.personalization.datamodel.NumberOfLiverMetastases
import com.hartwig.actin.personalization.datamodel.PFSMeasureType
import com.hartwig.actin.personalization.datamodel.RadiotherapyType
import com.hartwig.actin.personalization.datamodel.ReasonRefrainmentFromTumorDirectedTreatment
import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.datamodel.SurgeryCircumferentialResectionMargin
import com.hartwig.actin.personalization.datamodel.SurgeryRadicality
import com.hartwig.actin.personalization.datamodel.SurgeryTechnique
import com.hartwig.actin.personalization.datamodel.SurgeryType
import com.hartwig.actin.personalization.datamodel.SurgeryUrgency
import com.hartwig.actin.personalization.datamodel.TumorBasisOfDiagnosis
import com.hartwig.actin.personalization.datamodel.TumorDifferentiationGrade
import com.hartwig.actin.personalization.datamodel.TumorLocationCategory
import com.hartwig.actin.personalization.datamodel.TumorRegression
import com.hartwig.actin.personalization.datamodel.TumorType
import com.hartwig.actin.personalization.datamodel.VenousInvasionCategory
import com.hartwig.actin.personalization.datamodel.VitalStatus
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrAnastomoticLeakageAfterSurgeryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrAnorectalVergeDistanceCategoryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrAsaClassificationPreSurgeryOrEndoscopyMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrBooleanMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrCciNumberOfCategoriesMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrDistantMetastasesStatusMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrExtraMuralInvasionCategoryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrGastroenterologyResectionTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrLymphaticInvasionCategoryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrNumberOfLiverMetastasesMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrPFSMeasureTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrRadiotherapyTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrReasonRefrainmentFromTumorDirectedTherapyMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrSexMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrSurgeryCircumferentialResectionMarginMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrSurgeryRadicalityMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrSurgeryTechniqueMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrSurgeryTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrSurgeryUrgencyMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorBasisOfDiagnosisMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorDifferentiationGradeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorLocationCategoryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorRegressionMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrVenousInvasionCategoryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrVitalStatusMapper

inline fun <reified T> resolve(code: Int): T {
    return when (T::class) {
        AnastomoticLeakageAfterSurgery::class -> NcrAnastomoticLeakageAfterSurgeryMapper
        AnorectalVergeDistanceCategory::class -> NcrAnorectalVergeDistanceCategoryMapper
        AsaClassificationPreSurgeryOrEndoscopy::class -> NcrAsaClassificationPreSurgeryOrEndoscopyMapper
        Boolean::class -> NcrBooleanMapper
        CciNumberOfCategories::class -> NcrCciNumberOfCategoriesMapper
        DistantMetastasesStatus::class -> NcrDistantMetastasesStatusMapper
        ExtraMuralInvasionCategory::class -> NcrExtraMuralInvasionCategoryMapper
        GastroenterologyResectionType::class -> NcrGastroenterologyResectionTypeMapper
        LymphaticInvasionCategory::class -> NcrLymphaticInvasionCategoryMapper
        NumberOfLiverMetastases::class -> NcrNumberOfLiverMetastasesMapper
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

inline fun <reified T : U?, reified U> resolve(code: Int?): T = code?.let { resolve<U>(it) } as T