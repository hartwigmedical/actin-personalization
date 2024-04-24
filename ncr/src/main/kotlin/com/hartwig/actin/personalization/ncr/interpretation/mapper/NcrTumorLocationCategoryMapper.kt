package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.TumorLocationCategory
import com.hartwig.actin.personalization.ncr.interpretation.NcrIntCodeMapper

object NcrTumorLocationCategoryMapper : NcrIntCodeMapper<TumorLocationCategory> {

    override fun resolve(code: Int): TumorLocationCategory {
        return when (code) {
            100000 -> TumorLocationCategory.HEAD_AND_NECK
            200000 -> TumorLocationCategory.DIGESTIVE_TRACT
            300000 -> TumorLocationCategory.RESPIRATORY_TRACT
            400000 -> TumorLocationCategory.SKIN
            450000 -> TumorLocationCategory.BONE_CARTILAGE_AND_SOFT_TISSUE
            500000 -> TumorLocationCategory.BREAST
            600000 -> TumorLocationCategory.FEMALE_REPRODUCTIVE_SYSTEM
            700000 -> TumorLocationCategory.MALE_REPRODUCTIVE_SYSTEM
            710000 -> TumorLocationCategory.URINARY_TRACT
            800000 -> TumorLocationCategory.HEMATOLOGY
            900000 -> TumorLocationCategory.ENDOCRINE_GLAND
            910000 -> TumorLocationCategory.EYE
            960000 -> TumorLocationCategory.CENTRAL_NERVOUS_SYSTEM
            990000 -> TumorLocationCategory.OTHER
            else -> throw IllegalArgumentException("Unknown TumorLocationCategory code: $code")
        }
    }
}
