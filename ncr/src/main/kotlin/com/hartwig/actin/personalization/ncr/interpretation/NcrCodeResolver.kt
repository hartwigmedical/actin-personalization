package com.hartwig.actin.personalization.ncr.interpretation

import com.hartwig.actin.personalization.ncr.datamodel.AnastomoticLeakageAfterSurgery
import com.hartwig.actin.personalization.ncr.datamodel.AnorectalVergeDistanceCategory
import com.hartwig.actin.personalization.ncr.datamodel.AsaClassificationPreSurgeryOrEndoscopy
import com.hartwig.actin.personalization.ncr.datamodel.CciNumberOfCategories
import com.hartwig.actin.personalization.ncr.datamodel.DistantMetastasesStatus
import com.hartwig.actin.personalization.ncr.datamodel.ExtraMuralInvasionCategory
import com.hartwig.actin.personalization.ncr.datamodel.GastroenterologyResectionType
import com.hartwig.actin.personalization.ncr.datamodel.LymphaticInvasionCategory
import com.hartwig.actin.personalization.ncr.datamodel.PFSMeasureType
import com.hartwig.actin.personalization.ncr.datamodel.RadiotherapyType
import com.hartwig.actin.personalization.ncr.datamodel.Sex
import com.hartwig.actin.personalization.ncr.datamodel.SurgeryCircumferentialResectionMargin
import com.hartwig.actin.personalization.ncr.datamodel.SurgeryRadicality
import com.hartwig.actin.personalization.ncr.datamodel.SurgeryTechnique
import com.hartwig.actin.personalization.ncr.datamodel.SurgeryType
import com.hartwig.actin.personalization.ncr.datamodel.SurgeryUrgency
import com.hartwig.actin.personalization.ncr.datamodel.TumorBasisOfDiagnosis
import com.hartwig.actin.personalization.ncr.datamodel.TumorDifferentiationGrade
import com.hartwig.actin.personalization.ncr.datamodel.TumorLocationCategory
import com.hartwig.actin.personalization.ncr.datamodel.TumorRegression
import com.hartwig.actin.personalization.ncr.datamodel.TumorType
import com.hartwig.actin.personalization.ncr.datamodel.VenousInvasionCategory
import com.hartwig.actin.personalization.ncr.datamodel.VitalStatus
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrAnastomoticLeakageAfterSurgeryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrAnorectalVergeDistanceCategoryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrAsaClassificationPreSurgeryOrEndoscopyMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrCciNumberOfCategoriesMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrDistantMetastasesStatusMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrExtraMuralInvasionCategoryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrGastroenterologyResectionTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrLymphaticInvasionCategoryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrPFSMeasureTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrRadiotherapyTypeMapper
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