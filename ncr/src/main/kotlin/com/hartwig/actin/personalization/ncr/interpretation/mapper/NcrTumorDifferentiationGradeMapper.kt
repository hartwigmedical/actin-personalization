package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.TumorDifferentiationGrade

object NcrTumorDifferentiationGradeMapper : NcrIntCodeMapper<TumorDifferentiationGrade?> {

    override fun resolve(code: Int): TumorDifferentiationGrade? {
        return when (code) {
            1 -> TumorDifferentiationGrade.GRADE_1_OR_WELL_DIFFERENTIATED
            2 -> TumorDifferentiationGrade.GRADE_2_OR_MODERATELY_DIFFERENTIATED
            3 -> TumorDifferentiationGrade.GRADE_3_OR_POORLY_DIFFERENTIATED
            4 -> TumorDifferentiationGrade.GRADE_4_OR_UNDIFFERENTIATED_OR_ANAPLASTIC_OR_GGG4
            9 -> null
            else -> throw IllegalArgumentException("Unknown TumorDifferentiationGrade code: $code")
        }
    }
}
