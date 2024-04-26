package com.hartwig.actin.personalization.ncr.interpretation

import com.hartwig.actin.personalization.datamodel.CciNumberOfCategories
import com.hartwig.actin.personalization.datamodel.DistantMetastasesStatus
import com.hartwig.actin.personalization.datamodel.GastroenterologyResectionType
import com.hartwig.actin.personalization.datamodel.MetastasesRadiotherapyType
import com.hartwig.actin.personalization.datamodel.MetastasesSurgeryType
import com.hartwig.actin.personalization.datamodel.NumberOfLiverMetastases
import com.hartwig.actin.personalization.datamodel.PfsMeasureType
import com.hartwig.actin.personalization.datamodel.RadiotherapyType
import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.datamodel.SurgeryType
import com.hartwig.actin.personalization.datamodel.TreatmentName
import com.hartwig.actin.personalization.datamodel.TumorBasisOfDiagnosis
import com.hartwig.actin.personalization.datamodel.TumorLocationCategory
import com.hartwig.actin.personalization.datamodel.TumorRegression
import com.hartwig.actin.personalization.datamodel.TumorType
import com.hartwig.actin.personalization.datamodel.VitalStatus
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrCciNumberOfCategoriesMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrDistantMetastasesStatusMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrGastroenterologyResectionTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrMetastasesRadiotherapyTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrMetastasesSurgeryTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrNumberOfLiverMetastasesMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrPfsMeasureTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrRadiotherapyTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrSexMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrSurgeryTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTreatmentNameMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorBasisOfDiagnosisMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorLocationCategoryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorRegressionMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrVitalStatusMapper

interface NcrIntCodeMapper<T> {
    fun resolve(code: Int): T

    fun resolve(code: Int?): T? {
        return code?.let(this::resolve)
    }
}

interface NcrStringCodeMapper<T> {
    fun resolve(code: String): T

    fun resolveNullable(code: String?): T? {
        return code?.let(this::resolve)
    }
}

inline fun <reified T> resolve(code: Int): T {
    return when (T::class) {
        CciNumberOfCategories::class -> NcrCciNumberOfCategoriesMapper
        DistantMetastasesStatus::class -> NcrDistantMetastasesStatusMapper
        GastroenterologyResectionType::class -> NcrGastroenterologyResectionTypeMapper
        NumberOfLiverMetastases::class -> NcrNumberOfLiverMetastasesMapper
        PfsMeasureType::class -> NcrPfsMeasureTypeMapper
        RadiotherapyType::class -> NcrRadiotherapyTypeMapper
        Sex::class -> NcrSexMapper
        SurgeryType::class -> NcrSurgeryTypeMapper
        TumorBasisOfDiagnosis::class -> NcrTumorBasisOfDiagnosisMapper
        TumorLocationCategory::class -> NcrTumorLocationCategoryMapper
        TumorRegression::class -> NcrTumorRegressionMapper
        TumorType::class -> NcrTumorTypeMapper
        VitalStatus::class -> NcrVitalStatusMapper
        else -> throw IllegalArgumentException("No Int code resolver found for ${T::class.simpleName}")
    }.resolve(code) as T
}

inline fun <reified T : U?, reified U> resolve(code: Int?): T = code?.let { resolve<U>(it) } as T

inline fun <reified T> resolve(code: String): T {
    return when (T::class) {
        MetastasesRadiotherapyType::class -> NcrMetastasesRadiotherapyTypeMapper
        MetastasesSurgeryType::class -> NcrMetastasesSurgeryTypeMapper
        TreatmentName::class -> NcrTreatmentNameMapper
        else -> throw IllegalArgumentException("No String code resolver found for ${T::class.simpleName}")
    }.resolve(code) as T
}